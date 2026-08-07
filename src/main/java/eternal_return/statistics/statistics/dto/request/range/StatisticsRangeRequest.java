package eternal_return.statistics.statistics.dto.request.range;

import eternal_return.statistics.common.enums.RangeSideEnum;
import eternal_return.statistics.core.exception.enums.LogMessageEnum;
import eternal_return.statistics.statistics.exception.StatisticsException;

import java.util.EnumMap;

public record StatisticsRangeRequest(
        String label,
        EnumMap<RangeSideEnum, StatisticsRequestSide> range
) {
    public StatisticsRequestSide start() {
        return side(RangeSideEnum.START);
    }

    public StatisticsRequestSide end() {
        return side(RangeSideEnum.END);
    }

    /**
     * 요청 body 가 {@code range} 자체나 양 끝 구간, 혹은 그 안의 {@code tierEnum} 을 빼먹은 경우를 걸러낸다.
     *
     * <p>막지 않으면 {@code range.get(...)} 과 {@code EnumUtils.listBetween} 에서
     * {@link NullPointerException} 이 나 500 으로 나간다 — 클라이언트가 body 를 잘못 보낸 것이
     * 서버 장애로 기록되고, 스택트레이스까지 로그에 실린다.
     *
     * <p>접근 지점을 여기 하나로 모아둔 이유는 호출부마다 검사를 반복하지 않기 위해서다.
     */
    private StatisticsRequestSide side(RangeSideEnum key) {
        StatisticsRequestSide side = range == null ? null : range.get(key);

        if (side == null) {
            throw new StatisticsException.InvalidRange(
                    LogMessageEnum.NULL_VALUE.format("range." + key.name())
            );
        }

        if (side.tierEnum() == null) {
            throw new StatisticsException.InvalidRange(
                    LogMessageEnum.NULL_VALUE.format("range." + key.name() + ".tierEnum")
            );
        }

        return side;
    }
}
