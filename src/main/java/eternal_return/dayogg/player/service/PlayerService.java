package eternal_return.dayogg.player.service;

import eternal_return.dayogg.core.exception.child.SeasonException;
import eternal_return.dayogg.tier.enums.TierEnum;
import eternal_return.dayogg.player.Player;
import eternal_return.dayogg.player.client.PlayerSeasonApiResponse;
import eternal_return.dayogg.player.dto.PlayerDto;
import eternal_return.dayogg.player.dto.PlayerSeasonDto;
import eternal_return.dayogg.player.player_season.PlayerSeason;
import eternal_return.dayogg.player.player_season.PlayerSeasonRepository;
import eternal_return.dayogg.player.repository.PlayerDslRepository;
import eternal_return.dayogg.player.repository.PlayerRepository;
import eternal_return.dayogg.tier.TierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerDslRepository playerDslRepository;
    private final PlayerSeasonRepository playerSeasonRepository;
    private final PlayerApiService playerApiService;
    private final TierService tierService;

    @Transactional
    public void updateLevel(Long playerId, Integer level) {
        Player player = playerDslRepository.getActive(playerId);

        player.updateLevel(level);
        player.updateLastSearchTime();
    }

    @Transactional
    public PlayerDto getPlayerDto(Long playerId) {
        return PlayerDto.from(playerDslRepository.getActive(playerId));
    }

    @Transactional
    public Player fetchPlayer(String name) {
        String playerSearchId = playerApiService.requestPlayerSearchId(name);

        Player player = playerDslRepository.findActiveByName(name)
                .orElseGet(() -> new Player(name, playerSearchId));

        return playerRepository.save(player);
    }

    @Transactional
    public void fetchPlayerSeason(Long playerId, List<Integer> seasonIdList) {
        Player player = playerDslRepository.getActive(playerId);

        List<PlayerSeason> playerSeasonList = new ArrayList<>();

        for (Integer seasonId : seasonIdList) {
            PlayerSeasonApiResponse apiResponse = playerApiService.requestPlayerSeason(player.getSearchId(), seasonId);
            TierEnum tierEnum = tierService.resolveTier(seasonId, apiResponse.mmr());

            PlayerSeason playerSeason = playerDslRepository.findSeason(player.getId(), seasonId)
                    .orElseGet(() -> new PlayerSeason(player.getId(), seasonId));

            playerSeason.update(apiResponse, tierEnum);

            playerSeasonList.add(playerSeason);
        }

        playerSeasonRepository.saveAll(playerSeasonList);
    }

    @Transactional
    public PlayerSeasonDto getPlayerSeason(Long playerId, Integer seasonId) {
        Optional<PlayerSeason> optional = playerDslRepository.findSeason(playerId, seasonId);

        if (optional.isEmpty()) {
            throw new SeasonException.NotFound("getPlayerSeason")
                    .withContext("playerId", playerId)
                    .withContext("seasonId", seasonId);
        }

        return PlayerSeasonDto.from(optional.get());
    }

    @Transactional
    public void deletePlayer(Long playerId) {
        Player player = playerDslRepository.getActive(playerId);
        player.deleted();
    }
}
