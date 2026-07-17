package eternal_return.statistics.player.service.facade;

import eternal_return.statistics.core.annotation.service_logging.ServiceLogging;
import eternal_return.statistics.player.Player;
import eternal_return.statistics.player.dto.response.PlayerResponse;
import eternal_return.statistics.player.player_season.PlayerSeason;
import eternal_return.statistics.player.repository.PlayerDslRepository;
import eternal_return.statistics.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerInfoFacade {
    private final PlayerDslRepository playerDslRepository;
    private final PlayerService playerService;

    @ServiceLogging
    @Transactional
    public PlayerResponse playerInfo(String name) {
        Optional<Player> optionalPlayer = playerDslRepository.findActiveByName(name);

        if (optionalPlayer.isEmpty()) return PlayerResponse.onlyPlayer(playerService.fetchPlayer(name));

        Player player = optionalPlayer.get();
        List<PlayerSeason> playerSeasonList = playerDslRepository.findSeasonList(player.getId());

        return PlayerResponse.from(player, playerSeasonList);
    }
}
