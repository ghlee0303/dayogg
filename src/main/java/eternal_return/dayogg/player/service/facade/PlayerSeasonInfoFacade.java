package eternal_return.dayogg.player.service.facade;

import eternal_return.dayogg.battle_result.BattleResult;
import eternal_return.dayogg.battle_result.repository.BattleResultDslRepository;
import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.player.dto.response.PlayerSeasonDetailResponse;
import eternal_return.dayogg.player.exception.PlayerException;
import eternal_return.dayogg.player.player_season.PlayerSeason;
import eternal_return.dayogg.player.repository.PlayerDslRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerSeasonInfoFacade {
    private final PlayerDslRepository playerDslRepository;
    private final BattleResultDslRepository battleResultDslRepository;

    @ServiceLogging
    @Transactional
    public PlayerSeasonDetailResponse seasonInfo(
            Long playerId, Integer seasonId
    ) {
        PlayerSeason playerSeason = playerDslRepository.findSeason(playerId, seasonId)
                .orElseThrow(() -> new PlayerException.NotFound("seasonInfo")
                        .withContext("playerId", playerId)
                        .withContext("seasonId", seasonId)
                );

        Optional<BattleResult> optionalBattleResult =
                battleResultDslRepository.findLatestByMostPlayedCharacter(playerId, seasonId);

        if (optionalBattleResult.isEmpty()) return PlayerSeasonDetailResponse.of(playerSeason);

        return PlayerSeasonDetailResponse.of(playerSeason, optionalBattleResult.get());
    }
}
