package eternal_return.dayogg.battle_result.repository.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;
import eternal_return.dayogg.battle_result.repository.condition.BattleResultRangeCondition;
import eternal_return.dayogg.tier.enums.TierEnum;

import java.time.LocalDate;
import java.util.List;

import static eternal_return.dayogg.battle_result.QBattleResult.battleResult;

public class BattleResultPredicate {

    private BattleResultPredicate() {
    }

    public static BooleanExpression playerIdEq(Long playerId) {
        return playerId != null ? battleResult.playerId.eq(playerId) : null;
    }

    public static BooleanExpression seasonIdEq(Integer seasonId) {
        return seasonId != null ? battleResult.seasonId.eq(seasonId) : null;
    }

    public static BooleanExpression mmrBetween(Integer startMmr, Integer endMmr) {
        if (startMmr == null && endMmr == null) return null;
        if (startMmr == null) return battleResult.mmrBefore.loe(endMmr);
        if (endMmr == null) return battleResult.mmrBefore.goe(startMmr);
        return battleResult.mmrBefore.between(startMmr, endMmr);
    }

    public static BooleanExpression tierEnumIn(List<TierEnum> tierEnumList) {
        return (tierEnumList != null && !tierEnumList.isEmpty())
                ? battleResult.tierEnum.in(tierEnumList)
                : null;
    }

    public static BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) return null;
        if (startDate == null) return battleResult.startDtm.lt(endDate.plusDays(1).atStartOfDay());
        if (endDate == null) return battleResult.startDtm.goe(startDate.atStartOfDay());
        return battleResult.startDtm.goe(startDate.atStartOfDay())
                .and(battleResult.startDtm.lt(endDate.plusDays(1).atStartOfDay()));
    }

    public static BooleanExpression[] fromRangeCondition(BattleResultRangeCondition condition) {
        return new BooleanExpression[]{
                playerIdEq(condition.playerId()),
                seasonIdEq(condition.seasonId()),
                mmrBetween(condition.startMmr(), condition.endMmr()),
                tierEnumIn(condition.tierEnumList()),
                dateBetween(condition.startDate(), condition.endDate())
        };
    }
}