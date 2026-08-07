package eternal_return.dayogg.crawling;

import eternal_return.dayogg.battle_result.service.BattleResultService;
import eternal_return.dayogg.core.api.CrawlingService;
import eternal_return.dayogg.player.service.PlayerService;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CrawlingServiceTest {
    @Autowired
    private CrawlingService crawlingService;

    @Test
    void test() {
        String url = "https://dak.gg/er/routes/66339";

        Document document = crawlingService.fetchDocument(url);
    }
}
