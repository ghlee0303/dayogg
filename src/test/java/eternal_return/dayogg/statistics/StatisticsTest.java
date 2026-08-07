package eternal_return.dayogg.statistics;

import eternal_return.dayogg.statistics.dto.response.StatisticsResponse;
import eternal_return.dayogg.statistics.service.StatisticsAggregationService;
import eternal_return.dayogg.statistics.service.StatisticsService;
import eternal_return.dayogg.player.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class StatisticsTest {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private StatisticsAggregationService statisticsAggregationService;
    @Autowired
    private PlayerService playerService;

    final String testName = "이리와요루미아섬";

    @BeforeEach
    void setUp() {
//        userService.saveFromApi(testName);
    }

    @Test
    void getResponseTotal() {
        List<StatisticsResponse> sd = statisticsService.getBySeasonTotal(1L, 37);
    }

    @Test
    void fetchBattleStatistics_동시성() throws InterruptedException {
//        raceCondition(10, this::fetchBattleStatistics_currentName);
    }

}
