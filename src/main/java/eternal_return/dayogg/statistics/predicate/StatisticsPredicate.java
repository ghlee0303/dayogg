package eternal_return.dayogg.statistics.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;

import static eternal_return.dayogg.statistics.QStatistics.statistics;


public class StatisticsPredicate {

    private StatisticsPredicate() {
    }

    public static BooleanExpression essentialIdentifier(Long playerId, Integer seasonId) {
        return statistics.playerId.eq(playerId)
                .and(statistics.seasonId.eq(seasonId));
    }
}