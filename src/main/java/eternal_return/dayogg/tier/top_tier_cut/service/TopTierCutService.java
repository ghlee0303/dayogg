package eternal_return.dayogg.tier.top_tier_cut.service;

import eternal_return.dayogg.core.exception.child.SeasonException;
import eternal_return.dayogg.meta.meta.season.SeasonMeta;
import eternal_return.dayogg.meta.meta.top_tier_cut.TopTierCutInfo;
import eternal_return.dayogg.meta.meta.top_tier_cut.TopTierCutMeta;
import eternal_return.dayogg.tier.top_tier_cut.TopTierCut;
import eternal_return.dayogg.tier.top_tier_cut.repository.TopTierCutDslRepository;
import eternal_return.dayogg.tier.top_tier_cut.repository.TopTierCutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 상위 티어 커트라인 조회·저장 서비스.
 *
 * <p>스케줄러({@link eternal_return.dayogg.tier.top_tier_cut.TopTierCutScheduler})에 의해
 * 주기적으로 호출되어 최신 커트라인을 수집·저장한다.
 * <br>티어 계산 시({@link eternal_return.dayogg.tier.TierService})에는
 * 게임 플레이 시각 기준으로 가장 적절한 커트라인을 조회한다.
 */
@Service
@RequiredArgsConstructor
public class TopTierCutService {

    private final TopTierCutRepository topTierCutRepository;
    private final TopTierCutDslRepository topTierCutDslRepository;
    private final TopTierCutApiService topTierCutApiService;

    private final TopTierCutMeta topTierCutMeta;
    private final SeasonMeta seasonMeta;

    /**
     * 조회 지연 시간 (분) — 스케줄 주기와 동일하게 설정
     */
    @Value("${scheduler.tier-cut.delay-minutes}")
    private int searchDelay;

    @Transactional
    public TopTierCut getTopTierCut(
            Integer seasonId,
            LocalDateTime playDateTime
    ) {
        Optional<TopTierCut> optionalSearchDelay = topTierCutDslRepository.cacheFindLatestUnderDelay(playDateTime, searchDelay);
        if (optionalSearchDelay.isPresent()) return optionalSearchDelay.get();

        Optional<TopTierCut> optionalOnlyDate = topTierCutDslRepository.findByOnlyDate(playDateTime);

        return optionalOnlyDate.orElseGet(() -> getDefaultTopTierCut(seasonId));
    }

    public TopTierCut getDefaultTopTierCut(Integer seasonId) {
        TopTierCutInfo info = topTierCutMeta.getBySeasonId(seasonId);

        if (info == null) {
            throw new SeasonException.NotFound("topTierCutMeta get seasonId null");
        }

        return new TopTierCut(info);
    }

    @Transactional
    public void fetchTopTierCut() {
        TopTierCutInfo info = topTierCutMeta.getBySeasonId(seasonMeta.getNowSeason().seasonId());
        if (info == null) return;

        Optional<TopTierCut> optionalNew = topTierCutApiService.requestTopTierCut(info);
        if (optionalNew.isEmpty()) return;

        topTierCutRepository.save(optionalNew.get());
    }
}