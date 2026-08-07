package eternal_return.dayogg.battle_result;

import eternal_return.dayogg.battle_result.client.BattleResultApiResponse;
import eternal_return.dayogg.battle_result.dto.BattleResultApiResult;
import eternal_return.dayogg.battle_result.service.BattleResultApiService;
import eternal_return.dayogg.player.dto.PlayerDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class BattleResultApiServiceTest {
    @Autowired
    BattleResultApiService battleResultApiService;

    List<String> testNameList = Arrays.asList(
            "Sj0m_t4lZ3aIuUZdhGUUD_GdurufD9-B8Vd6CNdAHlnpf74kltZl0nA9xyOr",                     // 페이블
            "UjoU0RlCdvslOeOXExmsl6i_WEUP3svuDK4nIJwj5D3Bhxg9ebb7MJZqqYSG2z3kga4mIbbs04RgsVY"   // 이리와요루미아섬
    );

    @Test
    void test() {
        BattleResultApiResult result = battleResultApiService.fetchGame(playerDto(testNameList.get(1)), null);

    }

    PlayerDto playerDto(String searchId) {
        return new PlayerDto(
                1L, searchId, 1, "", null
        );
    }
}
