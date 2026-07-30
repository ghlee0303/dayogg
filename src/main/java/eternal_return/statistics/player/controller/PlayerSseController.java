package eternal_return.statistics.player.controller;

import eternal_return.statistics.core.annotation.controller_logging.ControllerLogging;
import eternal_return.statistics.core.sse.SseJobDispatcher;
import eternal_return.statistics.player.service.PlayerService;
import eternal_return.statistics.player.service.facade.PlayerInfoFacade;
import eternal_return.statistics.player.service.facade.PlayerRefreshFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/player/sse")
@RequiredArgsConstructor
public class PlayerSseController {
    private final PlayerRefreshFacade playerRefreshFacade;
    private final PlayerInfoFacade playerInfoFacade;
    private final SseJobDispatcher sseJobDispatcher;

    @GetMapping("/info")
    @ControllerLogging("player-info")
    public ResponseEntity<SseEmitter> sseFetchPlayerInfo(
            @RequestParam String name
    ) {
        SseEmitter sseEmitter = sseJobDispatcher.controllerJobStart(
                "player-info:" + name,
                () -> playerInfoFacade.playerInfo(name)
        );

        return ResponseEntity.ok(sseEmitter);
    }

    @GetMapping("/refresh")
    @ControllerLogging("player-refresh")
    public ResponseEntity<SseEmitter> sseFetchRefresh(
            @RequestParam Long playerId
    ) {
        SseEmitter sseEmitter = sseJobDispatcher.controllerJobStart(
                "player-refresh:" + playerId,
                () -> playerRefreshFacade.refresh(playerId)
        );

        return ResponseEntity.ok(sseEmitter);
    }
}
