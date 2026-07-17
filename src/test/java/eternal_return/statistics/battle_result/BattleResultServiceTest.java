package eternal_return.statistics.battle_result;

import eternal_return.statistics.battle_result.service.BattleResultService;
import eternal_return.statistics.player.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BattleResultServiceTest {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private BattleResultService battleResultService;


    @BeforeEach
    public void beforeAll() {
    }

    @Test
    void fetchNewBattleResult() {
    }
}
