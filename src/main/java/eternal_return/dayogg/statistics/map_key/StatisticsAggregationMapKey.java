package eternal_return.dayogg.statistics.map_key;

import eternal_return.dayogg.battle_result.BattleResult;
import eternal_return.dayogg.tier.enums.TierEnum;

public record StatisticsAggregationMapKey(
        int seasonId, int characterNum, int weaponNum, TierEnum tierEnum
) {
    public static StatisticsAggregationMapKey fromBattleResult(BattleResult battleResult) {
        TierEnum tierEnum = battleResult.getTierEnum();
        return new StatisticsAggregationMapKey(
                battleResult.getSeasonId(),
                battleResult.getCharacterNum(),
                battleResult.getBestWeapon(),
                tierEnum != null ? tierEnum : TierEnum.UNRANK
        );
    }
}
