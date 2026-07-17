package eternal_return.statistics.tier.top_tier_cut.repository;

import eternal_return.statistics.core.redis.RedisJsonStore;
import eternal_return.statistics.tier.top_tier_cut.TopTierCut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * {@link TopTierCut} Redis 캐시 저장소.
 *
 * <p>ZSET({@code top_tier_cut_score})에 수집 시각을 score로 저장하여
 * 특정 시각 범위의 커트라인을 빠르게 조회할 수 있도록 한다.
 *
 * <pre>
 * ZSET 구조:
 *   key   = "top_tier_cut_score"
 *   value = topTierCut.id (String)
 *   score = 수집 시각 (Unix timestamp ms)
 * </pre>
 */
@Repository
@RequiredArgsConstructor
public class TopTierCutRedisRepository {

    private static final String SCORE_KEY = "top_tier_cut_score";
    private static final String CACHE_KEY = "top_tier_cut_cache:";

    private final RedisJsonStore redisJsonStore;

    /**
     * 조회 지연 시간 (분) — 스케줄 주기와 동일하게 설정
     */
    @Value("${cache.timeout-minutes}")
    private int timeout;

    /**
     * ZSET에 커트라인 ID·score를 저장하고, JSON 값도 별도 키에 캐싱한다.
     *
     * @param topTierCut 저장할 커트라인
     */
    public void saveScore(TopTierCut topTierCut) {
        String key = createKey(topTierCut);
        long score = Timestamp.valueOf(topTierCut.getDateTime()).getTime();

        redisJsonStore.saveZSET(SCORE_KEY, key, score);
        redisJsonStore.save(key, topTierCut, Duration.ofMinutes(timeout));
    }

    public void saveScoreBySeasonFirst(TopTierCut topTierCut, Integer seasonId) {

    }

    private String createKey(TopTierCut topTierCut) {
        return CACHE_KEY + topTierCut.getId();
    }

    /**
     * 지정 시각으로부터 {@code searchDelay}분 이전까지의 범위에서
     * 가장 최근 커트라인을 조회한다.
     *
     * @param targetTime  기준 시각
     * @param searchDelay 탐색 범위 (분)
     * @return 범위 내 가장 최근 커트라인 (없으면 {@link Optional#empty()})
     */
    public Optional<TopTierCut> findScoreLatestUnderDelay(LocalDateTime targetTime, int searchDelay) {
        long minScore = Timestamp.valueOf(targetTime.minusMinutes(searchDelay)).getTime();
        long maxScore = Timestamp.valueOf(targetTime).getTime();

        // reverseRangeByScore: score 내림차순 → 가장 최근 1개
        List<String> idList = redisJsonStore.findZSET(SCORE_KEY, minScore, maxScore, 0, 1);

        if (idList.isEmpty()) return Optional.empty();

        return Optional.ofNullable(
                redisJsonStore.find(idList.getFirst(), TopTierCut.class)
        );
    }
}