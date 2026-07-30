package eternal_return.statistics.player.service.facade;

import eternal_return.statistics.battle_result.dto.BattleResultApiResult;
import eternal_return.statistics.battle_result.service.BattleResultService;
import eternal_return.statistics.common.log.LogContext;
import eternal_return.statistics.core.annotation.service_logging.LoggingType;
import eternal_return.statistics.core.annotation.service_logging.ServiceLogging;
import eternal_return.statistics.core.sse.SseJobResult;
import eternal_return.statistics.player.dto.PlayerDto;
import eternal_return.statistics.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerRefreshFacade {
    private final PlayerService playerService;
    private final BattleResultService battleResultService;
    private final TransactionTemplate transactionTemplate;

    @ServiceLogging(loggingType = LoggingType.PARENT)
    public SseJobResult refresh(Long playerId) {
        LogContext.put("playerId", playerId);

        PlayerDto playerDto = playerService.getPlayerDto(playerId);
        BattleResultApiResult apiResult = battleResultService.fetchNewBattleResult(playerDto);

        if (apiResult.isPresent()) {
            transactionTemplate.executeWithoutResult(status -> {
                battleResultService.saveBattleResult(playerDto.id(), apiResult.getResponseList());
                playerService.updateAfterRefresh(playerDto.id(), apiResult.getPlayerLevel());
                playerService.fetchPlayerSeason(playerDto.id(), apiResult.getSeasonIdList());
            });
        }

        return new SseJobResult(playerDto.id().toString());
    }
}
