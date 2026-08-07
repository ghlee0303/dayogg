package eternal_return.dayogg.tier.top_tier_cut;

import eternal_return.dayogg.tier.top_tier_cut.service.TopTierCutService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopTierCutScheduler {

    private final TopTierCutService topTierCutService;

    @Scheduled(
            fixedDelayString = "PT${scheduler.tier-cut.delay-minutes}M",
            initialDelayString = "PT0S"
    )
    public void collectTopTierCut() {
        topTierCutService.fetchTopTierCut();
    }
}