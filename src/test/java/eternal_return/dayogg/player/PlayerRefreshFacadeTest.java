package eternal_return.dayogg.player;

import eternal_return.dayogg.core.sse.SseJobResult;
import eternal_return.dayogg.player.dto.PlayerDto;
import eternal_return.dayogg.player.service.PlayerService;
import eternal_return.dayogg.support.race_condtion.RaceCondition;
import eternal_return.dayogg.support.race_condtion.RaceJob;
import eternal_return.dayogg.player.service.facade.PlayerRefreshFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class PlayerRefreshFacadeTest {
    @Autowired
    PlayerService playerService;
    @Autowired
    PlayerRefreshFacade playerRefreshFacade;
    @Autowired
    RaceCondition raceCondition;

    private List<String> testNameList = Arrays.asList(
            "고양이는고양고양",
            "oracle1"
//            "페이블",
//            "정진호",
//            "한동그라미",
//            "커리",
//            "바르지않은닉네임",
//            "이리와요루미아섬",
//            "아기망나니",
//            "작살",
//            "NXA",
//            "Kiro1",
//            "정누리"
    );

    private SseJobResult refresh(String name) {
        Player player = playerService.fetchPlayer(name);
        return playerRefreshFacade.refresh(player.getId());
    }

    @Test
    public void single() {
        refresh("페이블");
    }

    @Test
    public void refresh_race_condition() {
        List<RaceJob<String, SseJobResult>> jobList = RaceJob.createList(testNameList, 10L, 15L);

        raceCondition.start(
                jobList,
                this::refresh
        );
        for (RaceJob<String, SseJobResult> job : jobList) {
            assertThat(job.getException()).isNull();
            assertThat(job.getResult()).isNotNull();
        }
    }
}
