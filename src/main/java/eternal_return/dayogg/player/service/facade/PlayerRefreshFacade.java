package eternal_return.dayogg.player.service.facade;

import eternal_return.dayogg.battle_result.dto.BattleResultApiResult;
import eternal_return.dayogg.battle_result.service.BattleResultService;
import eternal_return.dayogg.common.log.LogContext;
import eternal_return.dayogg.core.annotation.service_logging.LoggingType;
import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.core.sse.SseJobResult;
import eternal_return.dayogg.player.dto.PlayerDto;
import eternal_return.dayogg.player.service.PlayerService;
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

        transactionTemplate.executeWithoutResult(status -> {
            playerService.updateLevel(playerDto.id(), apiResult.getPlayerLevel());
            playerService.fetchPlayerSeason(playerDto.id(), apiResult.getSeasonIdList());
            battleResultService.saveBattleResult(playerDto.id(), apiResult.getResponseList());
        });

        return new SseJobResult(playerDto.id().toString());
    }
}
