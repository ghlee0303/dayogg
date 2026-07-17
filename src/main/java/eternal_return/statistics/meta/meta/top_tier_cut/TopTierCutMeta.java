package eternal_return.statistics.meta.meta.top_tier_cut;

import lombok.Getter;

import java.util.Map;

@Getter
public class TopTierCutMeta {

    private final Map<Integer, TopTierCutInfo> map;

    public TopTierCutMeta(Map<Integer, TopTierCutInfo> map) {
        this.map = map;
    }

    public TopTierCutInfo getBySeasonId(Integer seasonId) {
        return map.get(seasonId);
    }
}
