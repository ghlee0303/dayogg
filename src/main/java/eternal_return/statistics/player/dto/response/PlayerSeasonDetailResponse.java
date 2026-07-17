package eternal_return.statistics.player.dto.response;

import eternal_return.statistics.battle_result.BattleResult;
import eternal_return.statistics.common.enums.ServerEnum;
import eternal_return.statistics.player.player_season.PlayerSeason;
import eternal_return.statistics.tier.enums.TierEnum;

public record PlayerSeasonDetailResponse(
        Integer seasonId, Integer mmr, Integer rank,
        Integer serverRank, ServerEnum serverEnum, TierEnum tierEnum,
        Integer mostCharacterNum, Integer latestSkinCode
) {
    /**
     * @param mostPlayedBattleResult 최다 플레이 캐릭터의 최근 전적
     */
    public static PlayerSeasonDetailResponse of(
            PlayerSeason playerSeason, BattleResult mostPlayedBattleResult
    ) {
        return new PlayerSeasonDetailResponse(
                playerSeason.getSeasonId(),
                playerSeason.getMmr(),
                playerSeason.getRank(),
                playerSeason.getServerRank(),
                playerSeason.getServerEnum(),
                playerSeason.getTierEnum(),
                mostPlayedBattleResult.getCharacterNum(),
                mostPlayedBattleResult.getSkinCode()
        );
    }

    /**
     * 전적이 없는 시즌의 응답. 캐릭터/스킨 정보는 null로 내려간다.
     */
    public static PlayerSeasonDetailResponse of(PlayerSeason playerSeason) {
        return new PlayerSeasonDetailResponse(
                playerSeason.getSeasonId(),
                playerSeason.getMmr(),
                playerSeason.getRank(),
                playerSeason.getServerRank(),
                playerSeason.getServerEnum(),
                playerSeason.getTierEnum(),
                null,
                null
        );
    }
}
