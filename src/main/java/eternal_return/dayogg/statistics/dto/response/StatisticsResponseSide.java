package eternal_return.dayogg.statistics.dto.response;

import eternal_return.dayogg.common.enums.RangeSideEnum;
import eternal_return.dayogg.common.utils.EnumUtils;
import eternal_return.dayogg.statistics.dto.request.range.StatisticsRequestSide;
import eternal_return.dayogg.statistics.range.RangeSide;
import eternal_return.dayogg.tier.enums.TierEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class StatisticsResponseSide extends RangeSide {
    protected TierEnum tierEnum;

    public StatisticsResponseSide(StatisticsRequestSide request) {
        this.tierEnum = request.tierEnum();
        this.mmr = request.mmr();
        this.dateTime = request.dateTime();
    }

    public void setMin(Integer mmr, LocalDateTime dateTime, TierEnum tierEnum) {
        super.setMin(mmr, dateTime);
        this.tierEnum = EnumUtils.min(this.tierEnum, tierEnum);
    }

    public void setMax(Integer mmr, LocalDateTime dateTime, TierEnum tierEnum) {
        super.setMax(mmr, dateTime);
        this.tierEnum = EnumUtils.max(this.tierEnum, tierEnum);
    }

    public static EnumMap<RangeSideEnum, StatisticsResponseSide> mapCreator() {
        EnumMap<RangeSideEnum, StatisticsResponseSide> rangeMap = new EnumMap<>(RangeSideEnum.class);

        for (RangeSideEnum sideEnum : RangeSideEnum.values()) {
            rangeMap.put(sideEnum, new StatisticsResponseSide());
        }

        return rangeMap;
    }

    public static void merge(
            Map<RangeSideEnum, StatisticsResponseSide> target,
            Integer mmr, LocalDateTime dateTime, TierEnum tierEnum
    ) {
        for (RangeSideEnum sideEnum : target.keySet()) {
            StatisticsResponseSide side = target.get(sideEnum);

            switch (sideEnum) {
                case START -> side.setMin(mmr, dateTime, tierEnum);
                case END -> side.setMax(mmr, dateTime, tierEnum);
            }
        }
    }

    public static void merge(
            Map<RangeSideEnum, StatisticsResponseSide> target,
            Map<RangeSideEnum, StatisticsResponseSide> source
    ) {
        for (RangeSideEnum sideEnum : source.keySet()) {
            StatisticsResponseSide tgt = target.get(sideEnum);
            StatisticsResponseSide src = source.get(sideEnum);

            switch (sideEnum) {
                case START -> tgt.setMin(src.getMmr(), src.getDateTime(), src.getTierEnum());
                case END -> tgt.setMax(src.getMmr(), src.getDateTime(), src.getTierEnum());
            }
        }
    }

    public static void merge(
            Map<RangeSideEnum, StatisticsResponseSide> target,
            Map<RangeSideEnum, ? extends RangeSide> source,
            TierEnum tierEnum
    ) {
        for (RangeSideEnum sideEnum : source.keySet()) {
            StatisticsResponseSide tgt = target.get(sideEnum);
            RangeSide src = source.get(sideEnum);

            switch (sideEnum) {
                case START -> tgt.setMin(src.getMmr(), src.getDateTime(), tierEnum);
                case END -> tgt.setMax(src.getMmr(), src.getDateTime(), tierEnum);
            }
        }
    }
}
