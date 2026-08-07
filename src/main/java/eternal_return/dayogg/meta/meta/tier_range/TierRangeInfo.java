package eternal_return.dayogg.meta.meta.tier_range;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eternal_return.dayogg.tier.enums.TierEnum;
import lombok.Getter;

@Getter
public class TierRangeInfo {

    private final TierEnum tierEnum;
    private final int step;
    private final int gap;
    private final int start;
    private final int end;

    /**
     * JSON 역직렬화용 생성자
     */
    @JsonCreator
    public TierRangeInfo(
            @JsonProperty("step") int step,
            @JsonProperty("gap") int gap,
            @JsonProperty("start") int start,
            @JsonProperty("end") int end
    ) {
        this(null, step, gap, start, end);
    }

    public TierRangeInfo(TierEnum tierEnum, int start, int end) {
        this(tierEnum, 1, 0, start, end);
    }

    private TierRangeInfo(TierEnum tierEnum, int step, int gap, int start, int end) {
        this.tierEnum = tierEnum;
        this.step = step;
        this.gap = gap;
        this.start = start;
        this.end = end;
    }

    public static TierRangeInfo Unrank() {
        return new TierRangeInfo(TierEnum.UNRANK, 0, 0);
    }

    public TierRangeInfo withTierEnum(TierEnum tierEnum) {
        return new TierRangeInfo(tierEnum, this.step, this.gap, this.start, this.end);
    }

    public TierRangeInfo withEnd(int end) {
        return new TierRangeInfo(this.tierEnum, this.step, this.gap, this.start, end);
    }
}
