package eternal_return.statistics.player.controller;

import eternal_return.statistics.player.dto.response.PlayerSeasonDetailResponse;
import eternal_return.statistics.player.service.PlayerService;
import eternal_return.statistics.player.service.facade.PlayerSeasonInfoFacade;
import eternal_return.statistics.statistics.dto.response.StatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerSeasonInfoFacade playerSeasonInfoFacade;

    @GetMapping("/season")
    public ResponseEntity<PlayerSeasonDetailResponse> getSeasonInfo(
            @RequestParam Long playerId,
            @RequestParam Integer seasonId
    ) {
        return ResponseEntity.ok(
                playerSeasonInfoFacade.seasonInfo(playerId, seasonId)
        );
    }
}
