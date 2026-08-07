package eternal_return.dayogg.tier;

import eternal_return.dayogg.core.annotation.controller_logging.ControllerLogging;
import eternal_return.dayogg.meta.meta.season.SeasonMeta;
import eternal_return.dayogg.meta.meta.tier_range.TierRangeInfo;
import eternal_return.dayogg.tier.enums.TierEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/tier")
@RequiredArgsConstructor
public class TierController {
    private final TierService tierService;
    private final SeasonMeta seasonMeta;

    @GetMapping("/range")
    @ControllerLogging
    public ResponseEntity<Map<TierEnum, TierRangeInfo>> getTierRange(
            @RequestParam(required = false) Integer seasonId,
            @RequestParam(required = false) LocalDateTime dateTime
    ) {
        Integer targetSeasonId = seasonId == null ? seasonMeta.getNowSeason().seasonId() : seasonId;

        return ResponseEntity.ok(
                tierService.getTierRangeByDate(targetSeasonId, dateTime).getTierMetaMap()
        );
    }
}
