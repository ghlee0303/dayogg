package eternal_return.statistics.player;

import eternal_return.statistics.player.service.PlayerApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PlayerApiServiceTest {
    @Autowired
    private PlayerApiService playerApiService;

    @Test
    public void requestPlayerInfo() {
    }
}
