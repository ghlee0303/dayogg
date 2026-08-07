package eternal_return.dayogg.statistics;

import eternal_return.dayogg.core.file.JsonFileService;
import eternal_return.dayogg.statistics.dto.response.StatisticsResponse;
import eternal_return.dayogg.statistics.service.StatisticsService;
import eternal_return.dayogg.player.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@SpringBootTest
public class StatisticsJsonTest {
    @Autowired
    JsonFileService jsonFileService;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    StatisticsService statisticsService;
    @Autowired
    PlayerService playerService;

    @Test
    @Rollback(value = false)
    void createJson() {
        List<StatisticsResponse> sd = statisticsService.getBySeasonTotal(1L, 37);

        jsonFileService.saveJsonFile(objectMapper.writeValueAsString(sd), "20260401_215.json");
    }

}
