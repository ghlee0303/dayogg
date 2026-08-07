package eternal_return.dayogg.player.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eternal_return.dayogg.player.Player;
import eternal_return.dayogg.player.exception.PlayerException;
import eternal_return.dayogg.player.player_season.PlayerSeason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static eternal_return.dayogg.player.QPlayer.player;
import static eternal_return.dayogg.player.player_season.QPlayerSeason.playerSeason;

@Repository
@RequiredArgsConstructor
public class PlayerDslRepository {
    private final JPAQueryFactory queryFactory;

    public Optional<Player> findActiveByName(String name) {
        return Optional.ofNullable(queryFactory
                .selectFrom(player)
                .where(
                        player.name.eq(name),
                        player.isDeleted.eq(false)
                ).fetchOne()
        );
    }

    public Optional<Player> findActive(Long playerId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(player)
                .where(
                        player.id.eq(playerId),
                        player.isDeleted.eq(false)
                ).fetchOne()
        );
    }

    public Player getActive(Long playerId) {
        return findActive(playerId).orElseThrow(() ->
                new PlayerException.NotFound("PlayerDslRepository.getActive")
                        .withContext("playerId", playerId)
        );
    }

    public Optional<PlayerSeason> findSeason(Long playerId, Integer seasonId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(playerSeason)
                .where(
                        playerSeason.playerId.eq(playerId),
                        playerSeason.seasonId.eq(seasonId)
                )
                .fetchOne()
        );
    }

    public List<PlayerSeason> findSeasonList(Long playerId) {
        return queryFactory
                .selectFrom(playerSeason)
                .where(
                        playerSeason.playerId.eq(playerId)
                )
                .orderBy(playerSeason.seasonId.desc())
                .fetch();
    }
}
