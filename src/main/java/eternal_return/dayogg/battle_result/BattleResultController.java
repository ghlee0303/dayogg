package eternal_return.dayogg.battle_result;

import eternal_return.dayogg.battle_result.dto.BattlePageResult;
import eternal_return.dayogg.battle_result.dto.BattleRangeResult;
import eternal_return.dayogg.battle_result.dto.request.range.BattleRangeRequest;
import eternal_return.dayogg.battle_result.service.BattleResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/battle")
@RequiredArgsConstructor
public class BattleResultController {
    private final BattleResultService battleResultService;

    @PostMapping("/range")
    public ResponseEntity<List<BattleRangeResult>> postRange(
            @RequestParam Long playerId,
            @RequestParam Integer seasonId,
            @RequestBody List<BattleRangeRequest> requestList
    ) {
        return ResponseEntity.ok(
                battleResultService.getByRange(playerId, seasonId, requestList)
        );
    }

    @PostMapping("/range/page")
    public ResponseEntity<BattlePageResult> postRangePage(
            @RequestParam Long playerId,
            @RequestParam Integer seasonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestBody BattleRangeRequest request
    ) {
        return ResponseEntity.ok(
                battleResultService.getPageByRange(playerId, seasonId, request, page, size)
        );
    }
}
