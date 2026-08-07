package eternal_return.dayogg.battle_result;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * {@link BattleResult} 기본 CRUD 레포지토리.
 */
@Repository
public interface BattleResultRepository extends JpaRepository<BattleResult, Long> {

}