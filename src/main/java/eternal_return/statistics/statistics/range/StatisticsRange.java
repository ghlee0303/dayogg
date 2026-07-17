package eternal_return.statistics.statistics.range;

import eternal_return.statistics.common.enums.RangeSideEnum;
import eternal_return.statistics.statistics.Statistics;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_statistics_range_statistics_side",
        columnNames = {"statistics_id", "side_enum"}
))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsRange extends RangeSide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statistics_id")
    private Statistics statistics;

    @Enumerated(EnumType.STRING)
    @Column(name = "side_enum")
    private RangeSideEnum sideEnum;

    public StatisticsRange(RangeSideEnum sideEnum, Statistics statistics) {
        this.sideEnum = sideEnum;
        this.statistics = statistics;
    }

    public static Map<RangeSideEnum, StatisticsRange> mapCreator(Statistics statistics) {
        Map<RangeSideEnum, StatisticsRange> rangeMap = new EnumMap<>(RangeSideEnum.class);

        for (RangeSideEnum sideEnum : RangeSideEnum.values()) {
            rangeMap.put(sideEnum, new StatisticsRange(sideEnum, statistics));
        }

        return rangeMap;
    }
}
