package eternal_return.statistics.player.repository;

import eternal_return.statistics.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByNameAndIsDeletedIsFalse(String name);
}
