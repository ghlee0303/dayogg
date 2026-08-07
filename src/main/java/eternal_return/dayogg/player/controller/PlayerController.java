package eternal_return.dayogg.player.controller;

import eternal_return.dayogg.player.dto.response.PlayerSeasonDetailResponse;
import eternal_return.dayogg.player.service.PlayerService;
import eternal_return.dayogg.player.service.facade.PlayerSeasonInfoFacade;
import eternal_return.dayogg.statistics.dto.response.StatisticsResponse;
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
