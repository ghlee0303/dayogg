package eternal_return.dayogg.statistics.condition;

import eternal_return.dayogg.battle_result.BattleResult;
import eternal_return.dayogg.tier.enums.TierEnum;

public record StatisticsUniqueCondition(
        Long playerId,
        Integer seasonId,
        TierEnum tierEnum,
        Integer characterNum,
        Integer weaponNum
) {
    public static StatisticsUniqueCondition fromBattleResult(BattleResult battleResult) {
        TierEnum tierEnum = battleResult.getTierEnum();
        return new StatisticsUniqueCondition(
                battleResult.getPlayerId(),
                battleResult.getSeasonId(),
                tierEnum != null ? tierEnum : TierEnum.UNRANK,
                battleResult.getCharacterNum(),
                battleResult.getBestWeapon()
        );
    }
}
