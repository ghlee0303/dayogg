package eternal_return.dayogg.battle_result.dto;

import eternal_return.dayogg.battle_result.dto.request.range.BattleRangeRequest;
import eternal_return.dayogg.battle_result.dto.request.range.BattleRequestSide;
import eternal_return.dayogg.common.enums.RangeSideEnum;
import lombok.Getter;

import java.util.EnumMap;
import java.util.List;

@Getter
public class BattleRangeResult {
    private final EnumMap<RangeSideEnum, BattleRequestSide> request;
    private final List<BattleResponse> games;

    public BattleRangeResult(BattleRangeRequest request, List<BattleResponse> games) {
        this.request = request.range();
        this.games = games;
    }
}
