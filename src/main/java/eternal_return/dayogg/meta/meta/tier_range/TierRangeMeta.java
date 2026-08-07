package eternal_return.dayogg.meta.meta.tier_range;

import eternal_return.dayogg.tier.enums.TierEnum;
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
