package eternal_return.statistics.statistics.dto.response;

import eternal_return.statistics.statistics.extend.AverageStatistics;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AverageStatisticsResponse extends AverageStatistics {
    protected float winRate;
    protected float top2Rate;
    protected float top3Rate;
    protected float avgMmrGain;
    protected float winDimensionRiftRate;
    protected float lateGameRate;           // 후반 생존률: 5일차 까지
    protected float creditUsageRate;       // 크레딧 효율

    @Override
    public void merge(AverageStatistics other) {
        super.merge(other);

        if (this.totalGames == 0) return;

        this.winRate = calculateAverage(this.wins);
        this.top2Rate = calculateAverage(this.top2);
        this.top3Rate = calculateAverage(this.top3);
        this.avgMmrGain = calculateAverage(this.sumMmrGain);
        this.winDimensionRiftRate = calculateAverage(this.winDimensionRiftCount, this.enterDimensionRiftCount);
        this.lateGameRate = calculateAverage(this.lateGameCount);
        this.creditUsageRate = calculateAverage(this.usedEpicMaterialCredits, this.usedCredits);
    }

    private float calculateAverage(Number value) {
        return value.floatValue() / this.totalGames;
    }

    private float calculateAverage(Number value, Number total) {
        return value.floatValue() / total.floatValue();
    }
}
