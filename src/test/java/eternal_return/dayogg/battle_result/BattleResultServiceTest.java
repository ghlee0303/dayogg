package eternal_return.dayogg.battle_result;

import eternal_return.dayogg.battle_result.dto.BattleResultApiResult;
import eternal_return.dayogg.battle_result.service.BattleResultService;
import eternal_return.dayogg.player.client.PlayerSeasonApiResponse;
import eternal_return.dayogg.player.dto.PlayerDto;
import eternal_return.dayogg.player.service.PlayerApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class BattleResultServiceTest {
    @Autowired
    private BattleResultService battleResultService;
    @Autowired
    private PlayerApiService playerApiService;

    List<String> testNameList = Arrays.asList(
            "Sj0m_t4lZ3aIuUZdhGUUD_GdurufD9-B8Vd6CNdAHlnpf74kltZl0nA9xyOr",                     // 페이블
            "UjoU0RlCdvslOeOXExmsl6i_WEUP3svuDK4nIJwj5D3Bhxg9ebb7MJZqqYSG2z3kga4mIbbs04RgsVY",  // 이리와요루미아섬
            "f441jZiK3oCl77MPXtjym4daTLPikdb37dNQ8-Hd86STSxkYw-7-kP24ReaRQQ"                    // 이준현99
    );

    @Test
    void fetchNewBattleResult() {
        BattleResultApiResult t1 = battleResultService.fetchNewBattleResult(playerDto(testNameList.get(2)));
    }

    @Test
    void requestPlayerSeason() {
        PlayerSeasonApiResponse season = playerApiService.requestPlayerSeason(testNameList.get(0), 39);
    }

    PlayerDto playerDto(String searchId) {
        return new PlayerDto(
                1L, searchId, 1, "", null
        );
    }
}
