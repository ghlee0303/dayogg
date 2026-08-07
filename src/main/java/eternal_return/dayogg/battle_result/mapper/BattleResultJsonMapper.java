package eternal_return.dayogg.battle_result.mapper;

import eternal_return.dayogg.battle_result.client.BattleResultApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임 API JSON 응답을 {@link BattleResultApiResponse} 목록으로 변환하는 매퍼.
 * 
 */
@Component
@RequiredArgsConstructor
public class BattleResultJsonMapper {

    private final ObjectMapper objectMapper;

    /**
     * API 응답 루트 노드에서 {@code userGames} 배열을 파싱하여 DTO 목록을 반환한다.
     *
     * @param root API 응답 전체 JSON 노드
     * @return 변환된 {@link BattleResultApiResponse} 목록 ({@code userGames}가 없으면 빈 목록)
     */
    public List<BattleResultApiResponse> battleResultApiDtoList(JsonNode root) {
        List<BattleResultApiResponse> dtoList = new ArrayList<>();

        JsonNode playerGames = root.get("userGames");
        if (playerGames == null) return dtoList;

        playerGames.iterator().forEachRemaining(game ->
                dtoList.add(objectMapper.treeToValue(game, BattleResultApiResponse.class))
        );

        return dtoList;
    }
}