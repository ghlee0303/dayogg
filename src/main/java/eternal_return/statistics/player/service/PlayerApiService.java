package eternal_return.statistics.player.service;

import eternal_return.statistics.core.api.ApiService;
import eternal_return.statistics.core.exception.BusinessException;
import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import eternal_return.statistics.core.exception.enums.LogMessageEnum;
import eternal_return.statistics.player.client.PlayerSeasonApiResponse;
import eternal_return.statistics.player.exception.PlayerException;
import eternal_return.statistics.tier.TierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerApiService {
    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    public String requestPlayerSearchId(String name) {
        String uri = "v1/user/nickname?query=" + name;
        JsonNode node = apiService.callApi(uri);
        notFoundValidation(node);

        return node
                .path("user")
                .path("userId")
                .asString();
    }

    public PlayerSeasonApiResponse requestPlayerSeason(String playerSearchId, Integer seasonId) {
        String uri = String.format("/v1/rank/uid/%s/%d/3", playerSearchId, seasonId);
        JsonNode node = apiService.callApi(uri);
        notFoundValidation(node);

        JsonNode playerRankNode = node.get("userRank");

        if (playerRankNode == null) return null;

        return objectMapper.treeToValue(playerRankNode, PlayerSeasonApiResponse.class);
    }

    private void notFoundValidation(JsonNode node) {
        int status = node.get("code").asInt();

        if (status == HttpStatus.NOT_FOUND.value()) {
            throw new PlayerException.NotFound("from Player Api");
        }
    }
}
