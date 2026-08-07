package eternal_return.dayogg.tier.dto;

import eternal_return.dayogg.tier.enums.TierEnum;
import eternal_return.dayogg.meta.meta.tier_range.TierRangeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TierRangeResult {
    private final Map<TierEnum, TierRangeInfo> tierMetaMap;
    private final List<TierEnum> sortedTierList;

    public TierRangeResult(
            Map<TierEnum, TierRangeInfo> tierMetaMap
    ) {
        this.tierMetaMap = tierMetaMap;
        this.sortedTierList = tierMetaMap.keySet().stream().sorted().toList();
    }

    public List<TierEnum> getMiddleTierList() {
        return sortedTierList.subList(1, sortedTierList.size() - 1);
    }

    public TierRangeInfo getMinTierRange() {
        return tierMetaMap.get(sortedTierList.getFirst());
    }

    public TierRangeInfo getMaxTierRange() {
        return tierMetaMap.get(sortedTierList.getLast());
    }

    public boolean isSingleTier() {
        return sortedTierList.size() == 1;
    }
}
