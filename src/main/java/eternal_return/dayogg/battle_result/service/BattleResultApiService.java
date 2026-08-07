package eternal_return.dayogg.battle_result.service;

import eternal_return.dayogg.battle_result.client.BattleResultApiResponse;
import eternal_return.dayogg.battle_result.dto.BattleResultApiResult;
import eternal_return.dayogg.battle_result.mapper.BattleResultJsonMapper;
import eternal_return.dayogg.meta.meta.season.SeasonInfo;
import eternal_return.dayogg.meta.meta.season.SeasonMeta;
import eternal_return.dayogg.core.api.ApiService;
import eternal_return.dayogg.player.dto.PlayerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleResultApiService {

    private final ApiService apiService;
    private final BattleResultJsonMapper battleResultJsonMapper;
    private final SeasonMeta seasonMeta;

    public BattleResultApiResult fetchGame(PlayerDto player, Integer lastBattleResultId) {
        BattleResultApiResult result = new BattleResultApiResult();
        Integer next = null;

        do {
            JsonNode json = apiService.callApi(buildGameUri(player.searchId(), next));
            next = extractNext(json);

            for (BattleResultApiResponse dto : battleResultJsonMapper.battleResultApiDtoList(json)) {
                SeasonInfo targetSeason = seasonMeta.getByDateTimeRange(dto.getStartDtm());

                result.updatePlayerInfo(dto);

                // 이미 저장된 게임에 도달하면 수집 종료
                if (dto.getGameId().equals(lastBattleResultId)) return result;
                // 현재 시즌 이외 게임에 도달하면 수집 종료
                if (!targetSeason.isCurrent()) return result;

                // 랭크 모드 외 스킵
                if (!dto.getMatchingMode().isRank()) continue;
                // 프리 시즌 게임 스킵
                if (targetSeason.isPreSeason()) continue;

                result.add(dto);
            }

        } while (next != null);

        return result;
    }

    /**
     * 전적 조회 URI를 생성한다.
     * {@code next}가 null이면 쿼리 파라미터를 추가하지 않는다.
     */
    private String buildGameUri(String playerSearchId, Integer next) {
        return UriComponentsBuilder
                .fromPath("v1/user/games/uid/{playerSearchId}")
                .queryParamIfPresent("next", Optional.ofNullable(next))
                .buildAndExpand(playerSearchId)
                .toUriString();
    }

    /**
     * 응답 JSON에서 다음 페이지 커서를 추출한다.
     * {@code next} 필드가 없거나 정수가 아니면 null을 반환한다.
     */
    private Integer extractNext(JsonNode json) {
        JsonNode nextNode = json.get("next");
        if (nextNode == null) return null;
        return nextNode.isInt() ? nextNode.asInt() : null;
    }
}