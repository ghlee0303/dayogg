package eternal_return.dayogg.statistics;

import eternal_return.dayogg.statistics.dto.request.range.StatisticsRangeRequest;
import eternal_return.dayogg.statistics.dto.response.StatisticsResponse;
import eternal_return.dayogg.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/season")
    public ResponseEntity<List<StatisticsResponse>> getSeasonTotal(
            @RequestParam Long playerId,
            @RequestParam(required = false) Integer seasonId
    ) {
        return ResponseEntity.ok(
                statisticsService.getBySeasonTotal(playerId, seasonId)
        );
    }

    @PostMapping("/range")
    public ResponseEntity<List<StatisticsResponse>> postRange(
            @RequestParam Long playerId,
            @RequestParam Integer seasonId,
            @RequestBody List<StatisticsRangeRequest> requestList
    ) {
        return ResponseEntity.ok(
                statisticsService.getByRange(playerId, seasonId, requestList)
        );
    }

}
