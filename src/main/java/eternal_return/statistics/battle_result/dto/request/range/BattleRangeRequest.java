package eternal_return.statistics.battle_result.dto.request.range;

import eternal_return.statistics.common.enums.RangeSideEnum;

import java.util.EnumMap;

public record BattleRangeRequest(
        EnumMap<RangeSideEnum, BattleRequestSide> range
) {
    public BattleRequestSide start() {
        return range.get(RangeSideEnum.START);
    }

    public BattleRequestSide end() {
        return range.get(RangeSideEnum.END);
    }
}
