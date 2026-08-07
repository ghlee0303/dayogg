package eternal_return.dayogg.statistics;

import eternal_return.dayogg.battle_result.service.BattleResultService;
import eternal_return.dayogg.statistics.repository.StatisticsDslRepository;
import eternal_return.dayogg.statistics.repository.StatisticsRepository;
import eternal_return.dayogg.statistics.service.StatisticsService;
import eternal_return.dayogg.player.service.PlayerService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class StatisticsMockTest {
    @Mock
    BattleResultService battleResultService;
    @Mock
    StatisticsRepository statisticsRepository;
    @Mock
    StatisticsDslRepository statisticsDslRepository;
    @Mock
    PlayerService playerService;
    @InjectMocks
    StatisticsService statisticsService;

//    @BeforeEach
//    void setUp() {
//        UserDto userDto = new UserDto(
//                1L,
//                "ASqFFdhlBRAhjHFQ_-rihnxfev9RZ3m-DVg1SXeY7lxyrS7UFe64lHosmzCyYLcHcnbrw2Oor85NnD0",
//                "이리와요루미아섬",
//                DateTimeUtils.toLocal("2026-03-10 09:30:59", null));
//        given(userService.findByNameAndUpdateSearchTime("이리와요루미아섬")).willReturn(userDto);
//    }
//
//    @Test
//    void fetchBattleStatistics_올바른_닉네임() {
//
//        statisticsService.fetchBattleStatistics("이리와요루미아섬");
//    }
}
