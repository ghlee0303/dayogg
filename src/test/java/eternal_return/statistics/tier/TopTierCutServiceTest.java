package eternal_return.statistics.tier;


import eternal_return.statistics.common.utils.DateTimeUtils;
import eternal_return.statistics.tier.top_tier_cut.TopTierCut;
import eternal_return.statistics.tier.top_tier_cut.service.TopTierCutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@SpringBootTest
public class TopTierCutServiceTest {
    @Autowired
    TopTierCutService topTierCutService;

    @Test
    void test1() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime localDateTime = DateTimeUtils.toLocal("2026-02-02 12:00:00", formatter);
//
//        Optional<TopTierCut> optional = topTierCutService.getTopTierCut(localDateTime);

    }
}
