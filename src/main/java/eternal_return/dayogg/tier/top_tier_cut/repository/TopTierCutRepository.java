package eternal_return.dayogg.tier.top_tier_cut.repository;

import eternal_return.dayogg.tier.top_tier_cut.TopTierCut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link TopTierCut} 기본 CRUD 레포지토리.
 * 동적 조회는 {@link TopTierCutDslRepository} 참조.
 */
@Repository
public interface TopTierCutRepository extends JpaRepository<TopTierCut, Long> {
}