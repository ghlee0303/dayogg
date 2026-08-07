package eternal_return.dayogg.statistics.repository;

import eternal_return.dayogg.statistics.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link Statistics} 기본 CRUD 레포지토리.
 * 동적 조회는 {@link StatisticsDslRepository} 참조.
 */
@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
}