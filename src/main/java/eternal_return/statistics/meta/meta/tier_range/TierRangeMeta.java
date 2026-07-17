package eternal_return.statistics.meta.meta.tier_range;

import eternal_return.statistics.tier.enums.TierEnum;
import lombok.Getter;

import java.util.Map;

@Getter
public class TierRangeMeta {

    private final Map<Integer, Map<TierEnum, TierRangeInfo>> map;

    public TierRangeMeta(Map<Integer, Map<TierEnum, TierRangeInfo>> map) {
        this.map = map;
    }

    public Map<TierEnum, TierRangeInfo> getBySeasonId(Integer seasonId) {
        return map.get(seasonId);
    }
}
