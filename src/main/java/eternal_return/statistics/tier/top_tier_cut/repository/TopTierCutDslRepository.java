package eternal_return.statistics.tier.top_tier_cut.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eternal_return.statistics.tier.top_tier_cut.TopTierCut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static eternal_return.statistics.tier.top_tier_cut.QTopTierCut.topTierCut;

/**
 * {@link TopTierCut} QueryDSL 조회 저장소.
 *
 * <p>Redis 캐시를 우선 조회하는 {@link #cacheFindLatestUnderDelay}를 통해
 * DB 부하를 최소화한다. 캐시 미스 시 DB에서 조회 후 Redis에 저장한다.
 */
@Repository
@RequiredArgsConstructor
public class TopTierCutDslRepository {

    private final JPAQueryFactory queryFactory;
    private final TopTierCutRedisRepository topTierCutRedisRepository;

    public Optional<TopTierCut> findLatestUnderDelay(LocalDateTime targetDateTime, int searchDelay) {
        LocalDateTime start = targetDateTime.minusMinutes(searchDelay);

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(topTierCut)
                        .where(topTierCut.dateTime.between(start, targetDateTime))
                        .orderBy(topTierCut.dateTime.desc())
                        .limit(1)
                        .fetchOne()
        );
    }

    public Optional<TopTierCut> findByOnlyDate(LocalDateTime targetDateTime) {
        LocalDateTime startOfDay = targetDateTime.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(topTierCut)
                        .where(topTierCut.dateTime.goe(startOfDay)
                                .and(topTierCut.dateTime.lt(endOfDay)))
                        .orderBy(topTierCut.dateTime.desc())
                        .limit(1)
                        .fetchOne()
        );
    }

    /**
     * Redis 캐시를 우선 조회하고, 캐시 미스 시 DB에서 조회 후 캐시에 저장한다.
     *
     * @param targetDateTime  기준 시각
     * @param searchDelay 탐색 범위 (분)
     */
    public Optional<TopTierCut> cacheFindLatestUnderDelay(LocalDateTime targetDateTime, int searchDelay) {
        Optional<TopTierCut> cached = topTierCutRedisRepository.findScoreLatestUnderDelay(targetDateTime, searchDelay);
        if (cached.isPresent()) return cached;

        Optional<TopTierCut> fromDb = findLatestUnderDelay(targetDateTime, searchDelay);
        if (fromDb.isEmpty()) return Optional.empty();

        // 캐시에 저장하고 DB 결과 반환
        topTierCutRedisRepository.saveScore(fromDb.get());
        return fromDb;
    }
}