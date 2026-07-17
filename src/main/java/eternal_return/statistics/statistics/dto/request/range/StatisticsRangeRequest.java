package eternal_return.statistics.statistics.dto.request.range;

import eternal_return.statistics.common.enums.RangeSideEnum;

import java.util.EnumMap;

public record StatisticsRangeRequest(
        String label,
        EnumMap<RangeSideEnum, StatisticsRequestSide> range
) {
    public StatisticsRequestSide start() {
        return range.get(RangeSideEnum.START);
    }

    public StatisticsRequestSide end() {
        return range.get(RangeSideEnum.END);
    }
}
