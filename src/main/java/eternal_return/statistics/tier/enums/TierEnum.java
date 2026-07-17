package eternal_return.statistics.tier.enums;

import eternal_return.statistics.common.enums.LocaleMappable;
import eternal_return.statistics.meta.meta.tier_range.TierRangeInfo;
import eternal_return.statistics.tier.exception.TierException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 이터널리턴의 랭크 티어를 나타내는 Enum
 * <p>
 * {@link TierRangeInfo}를 참조하여 {@link #calculate(Map, Integer)} 로 MMR 값에 해당하는 티어를 계산할 수 있다.
 *
 * @see TierRangeInfo
 */
public enum TierEnum implements LocaleMappable {
    UNRANK,
    IRON,
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND,
    METEORITE,
    MITHRIL,
    DEMIGOD,
    ETERNITY,
    ;

    public static TierEnum calculate(
            Map<TierEnum, TierRangeInfo> tierRangeMap, Integer mmr
    ) {
        if (mmr == null) {
            return null;
        }

        for (TierEnum tierEnum : tierRangeMap.keySet()) {
            TierRangeInfo tierRange = tierRangeMap.get(tierEnum);

            if (tierRange.getStart() <= mmr && mmr <= tierRange.getEnd()) {
                return tierEnum;
            }
        }

        throw new TierException.RangeOut("TierEnum");
    }

    public static List<TierEnum> calculateByRange(
            Map<TierEnum, TierRangeInfo> tierMetaMap, Integer startMmr, Integer endMmr
    ) {
        if (startMmr == null || endMmr == null) {
            return List.of();
        }

        List<TierEnum> result = new ArrayList<>();

        for (TierEnum tierEnum : tierMetaMap.keySet()) {
            TierRangeInfo tierRangeMeta = tierMetaMap.get(tierEnum);

            if (tierRangeMeta.getStart() < endMmr && tierRangeMeta.getEnd() > startMmr) {
                result.add(tierEnum);
            }
        }

        return result;
    }

    public boolean isRank() {
        return this != UNRANK;
    }

    @Override
    public String apiName() {
        return name();
    }

    @Override
    public String localeKey() {
        return name();
    }
}
