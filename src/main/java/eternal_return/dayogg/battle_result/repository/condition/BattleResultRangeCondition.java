package eternal_return.dayogg.battle_result.repository.condition;

import eternal_return.dayogg.tier.enums.TierEnum;

import java.time.LocalDate;
import java.util.List;

public record BattleResultRangeCondition(
        Long playerId, Integer seasonId,
        Integer startMmr, Integer endMmr,
        LocalDate startDate, LocalDate endDate,
        List<TierEnum> tierEnumList
) {
    public static BattleResultRangeCondition of(
            Long playerId, Integer seasonId
    ) {
        return new BattleResultRangeCondition(playerId, seasonId, null, null, null, null, null);
    }

    public static BattleResultRangeCondition of(
            Long playerId, Integer seasonId,
            LocalDate startDate, LocalDate endDate
    ) {
        return new BattleResultRangeCondition(playerId, seasonId, null, null, startDate, endDate, null);
    }

    public static BattleResultRangeCondition of(
            Long playerId, Integer seasonId,
            Integer startMmr, Integer endMmr,
            List<TierEnum> tierEnumList,
            LocalDate startDate, LocalDate endDate
    ) {
        return new BattleResultRangeCondition(playerId, seasonId, startMmr, endMmr, startDate, endDate, tierEnumList);
    }

    public static BattleResultRangeCondition of(
            Long playerId, Integer seasonId,
            TierEnum tierEnum,
            LocalDate startDate, LocalDate endDate
    ) {
        return new BattleResultRangeCondition(playerId, seasonId, null, null, startDate, endDate, List.of(tierEnum));
    }

    public static BattleResultRangeCondition of(
            Long playerId, Integer seasonId,
            List<TierEnum> tierEnumList,
            LocalDate startDate, LocalDate endDate
    ) {
        return new BattleResultRangeCondition(playerId, seasonId, null, null, startDate, endDate, tierEnumList);
    }

}
