package eternal_return.statistics.player.dto.response;

import eternal_return.statistics.player.Player;
import eternal_return.statistics.player.dto.PlayerDto;
import eternal_return.statistics.player.dto.PlayerSeasonDto;
import eternal_return.statistics.player.player_season.PlayerSeason;

import java.util.ArrayList;
import java.util.List;

public record PlayerResponse(
        PlayerDto player, List<PlayerSeasonDto> seasonList,
        Status status, Integer latestSeasonId
) {

    public static PlayerResponse from(
            Player player,
            List<PlayerSeason> seasonList
    ) {
        List<PlayerSeasonDto> seasonResponseList = new ArrayList<>();
        int latestSeasonId = 0;

        for (PlayerSeason playerSeason : seasonList) {
            latestSeasonId = Math.max(latestSeasonId, playerSeason.getSeasonId());
            seasonResponseList.add(PlayerSeasonDto.from(playerSeason));
        }

        return new PlayerResponse(
                PlayerDto.from(player),
                seasonResponseList,
                Status.OK,
                latestSeasonId
        );
    }

    public static PlayerResponse onlyPlayer(Player player) {
        return new PlayerResponse(
                PlayerDto.from(player),
                new ArrayList<>(),
                Status.CONTINUE,
                0
        );
    }

    private enum Status {
        OK, CONTINUE,
    }
}
