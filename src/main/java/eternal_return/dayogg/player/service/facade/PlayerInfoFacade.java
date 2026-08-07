package eternal_return.dayogg.player.service.facade;

import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.player.Player;
import eternal_return.dayogg.player.dto.response.PlayerResponse;
import eternal_return.dayogg.player.player_season.PlayerSeason;
import eternal_return.dayogg.player.repository.PlayerDslRepository;
import eternal_return.dayogg.player.service.PlayerService;
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
