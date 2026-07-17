package eternal_return.statistics.player.dto;

import eternal_return.statistics.common.enums.ServerEnum;
import eternal_return.statistics.tier.enums.TierEnum;
import eternal_return.statistics.player.player_season.PlayerSeason;

public record PlayerSeasonDto(
        Long id, Integer seasonId, Integer mmr, Integer rank,
        Integer serverRank, ServerEnum serverEnum, TierEnum tierEnum
) {

    public static PlayerSeasonDto from(PlayerSeason playerSeason) {
        return new PlayerSeasonDto(
                playerSeason.getId(),
                playerSeason.getSeasonId(),
                playerSeason.getMmr(),
                playerSeason.getRank(),
                playerSeason.getServerRank(),
                playerSeason.getServerEnum(),
                playerSeason.getTierEnum()
        );
    }
}
