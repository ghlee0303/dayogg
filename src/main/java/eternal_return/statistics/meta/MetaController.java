package eternal_return.statistics.meta;

import eternal_return.statistics.core.annotation.controller_logging.ControllerLogging;
import eternal_return.statistics.meta.enums.LanguageEnum;
import eternal_return.statistics.meta.enums.LocaleEnum;
import eternal_return.statistics.meta.meta.LocaleMeta;
import eternal_return.statistics.meta.meta.item.EquipInfo;
import eternal_return.statistics.meta.meta.item.ItemMeta;
import eternal_return.statistics.meta.meta.season.SeasonInfo;
import eternal_return.statistics.meta.meta.season.SeasonMeta;
import eternal_return.statistics.meta.meta.tier_range.TierRangeInfo;
import eternal_return.statistics.meta.meta.tier_range.TierRangeMeta;
import eternal_return.statistics.meta.meta.trait.TraitInfo;
import eternal_return.statistics.meta.meta.trait.TraitMeta;
import eternal_return.statistics.tier.enums.TierEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/meta")
@RequiredArgsConstructor
public class MetaController {
    private final LocaleMeta localeMeta;
    private final TierRangeMeta tierRangeMeta;
    private final SeasonMeta seasonMeta;
    private final TraitMeta traitMeta;
    private final ItemMeta itemMeta;

    @GetMapping("/equip")
    @ControllerLogging
    public ResponseEntity<Map<Integer, EquipInfo>> getEquip() {
        return ResponseEntity.ok(
                itemMeta.getEquipMap()
        );
    }

    @GetMapping("/locale")
    @ControllerLogging
    public ResponseEntity<Map<LocaleEnum, Map<String, String>>> getLocale(
            @RequestParam String hl
    ) {
        LanguageEnum enums = LanguageEnum.valueOf(hl);

        return ResponseEntity.ok(
                localeMeta.getByLanguage(enums)
        );
    }

    @GetMapping("/season")
    @ControllerLogging
    public ResponseEntity<List<SeasonInfo>> getSeason() {
        return ResponseEntity.ok(
                seasonMeta.getSeasonMetaList()
        );
    }

    @GetMapping("/trait")
    @ControllerLogging
    public ResponseEntity<List<TraitInfo>> getTrait() {
        return ResponseEntity.ok(
                traitMeta.getTraitList()
        );
    }

    @GetMapping("/tier_range")
    @ControllerLogging
    public ResponseEntity<Map<TierEnum, TierRangeInfo>> getTierRange(
            @RequestParam(required = false) Integer seasonId
    ) {
        return ResponseEntity.ok(
                tierRangeMeta.getBySeasonId(seasonId == null ? seasonMeta.getNowSeason().seasonId() : seasonId)
        );
    }
}
