package eternal_return.statistics.player.player_season;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerSeasonRepository extends JpaRepository<PlayerSeason, Long> {
}
