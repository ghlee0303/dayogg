package eternal_return.statistics.statistics.map_key;

import eternal_return.statistics.battle_result.BattleResult;
import eternal_return.statistics.tier.enums.TierEnum;

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
