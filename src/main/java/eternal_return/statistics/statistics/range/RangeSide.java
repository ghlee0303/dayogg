package eternal_return.statistics.statistics.range;

import eternal_return.statistics.common.enums.RangeSideEnum;
import eternal_return.statistics.common.utils.DateTimeUtils;
import eternal_return.statistics.common.utils.MathUtils;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class RangeSide {
    protected Integer mmr;
    protected LocalDateTime dateTime;

    public void setMin(Integer mmr, LocalDateTime dateTime) {
        this.mmr = MathUtils.min(mmr, this.mmr);
        this.dateTime = DateTimeUtils.min(dateTime, this.dateTime);
    }

    public void setMax(Integer mmr, LocalDateTime dateTime) {
        this.mmr = MathUtils.max(mmr, this.mmr);
        this.dateTime = DateTimeUtils.max(dateTime, this.dateTime);
    }

    public static void merge(
            Map<RangeSideEnum, ? extends RangeSide> range,
            Integer mmr, LocalDateTime dateTime
    ) {

        for (RangeSideEnum sideEnum : range.keySet()) {
            RangeSide side = range.get(sideEnum);
            switch (sideEnum) {
                case START -> side.setMin(mmr, dateTime);
                case END -> side.setMax(mmr, dateTime);
            }
        }
    }

}
