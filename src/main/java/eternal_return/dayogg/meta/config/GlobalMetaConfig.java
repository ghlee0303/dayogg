package eternal_return.dayogg.meta.config;

import eternal_return.dayogg.meta.service.SeasonService;
import eternal_return.dayogg.meta.service.EquipService;
import eternal_return.dayogg.tier.enums.TierEnum;
import eternal_return.dayogg.meta.service.LocaleService;
import eternal_return.dayogg.meta.meta.item.EquipInfo;
import eternal_return.dayogg.meta.meta.item.ItemInfo;
import eternal_return.dayogg.meta.meta.item.ItemMeta;
import eternal_return.dayogg.meta.meta.LocaleMeta;
import eternal_return.dayogg.meta.meta.phase.PhaseMeta;
import eternal_return.dayogg.meta.meta.phase.PhasePatchDto;
import eternal_return.dayogg.meta.meta.season.SeasonMeta;
import eternal_return.dayogg.meta.meta.tier_range.TierRangeInfo;
import eternal_return.dayogg.meta.meta.tier_range.TierRangeMeta;
import eternal_return.dayogg.meta.meta.top_tier_cut.TopTierCutInfo;
import eternal_return.dayogg.meta.meta.top_tier_cut.TopTierCutMeta;
import eternal_return.dayogg.meta.meta.trait.TraitInfo;
import eternal_return.dayogg.meta.meta.trait.TraitMeta;
import eternal_return.dayogg.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Configuration
@RequiredArgsConstructor
public class GlobalMetaConfig {
    private static final String TOP_TIER_CUT_DEFAULT_PATH = "meta/tier/top_tier_cut.json";
    private static final String TIER_RANGE_PATH = "meta/tier/tier_range.json";
    private static final String ITEM_PATH = "meta/item.json";
    private static final String PHASE_PATH = "meta/phase.json";
    private static final String TRAIT_PATH = "meta/trait.json";

    @Bean
    public SeasonMeta seasonMeta(SeasonService seasonService) {
        return new SeasonMeta(seasonService.getSeason());
    }

    @Bean
    public TopTierCutMeta topTierCutMeta(ObjectMapper objectMapper) {
        JsonNode root = JsonUtils.readJsonFileToNode(objectMapper, TOP_TIER_CUT_DEFAULT_PATH);
        Map<Integer, TopTierCutInfo> map = new HashMap<>();

        for (JsonNode entry : root) {
            TopTierCutInfo info = new TopTierCutInfo(
                    entry.get("demiGodRank").asInt(),
                    entry.get("demiGodCut").asInt(),
                    entry.get("eternityRank").asInt(),
                    entry.get("eternityCut").asInt()
            );

            // 같은 컷을 공유하는 시즌 ID들을 모두 등록
            for (JsonNode seasonId : entry.get("SeasonId")) {
                map.put(seasonId.asInt(), info);
            }
        }

        return new TopTierCutMeta(map);
    }

    @Bean
    public ItemMeta itemMeta(ObjectMapper objectMapper, EquipService equipService) {
        Map<Integer, ItemInfo> map = JsonUtils.readJsonFile(objectMapper, ITEM_PATH, new TypeReference<>() {
        });

        List<EquipInfo> equips = new ArrayList<>(equipService.getEquips());

        return new ItemMeta(map, equips);
    }

    @Bean
    public PhaseMeta phaseMeta(ObjectMapper objectMapper) {
        Map<Long, PhasePatchDto> source = JsonUtils.readJsonFile(objectMapper, PHASE_PATH, new TypeReference<>() {
        });
        return new PhaseMeta(source);
    }

    @Bean
    public TraitMeta traitMeta(ObjectMapper objectMapper) {
        List<TraitInfo> list = JsonUtils.readJsonFile(objectMapper, TRAIT_PATH, new TypeReference<>() {
        });
        return new TraitMeta(list);
    }

    @Bean
    public LocaleMeta localeMeta(LocaleService localeService) {
        return localeService.getL10n();
    }

    @Bean
    public TierRangeMeta tierRangeMeta(ObjectMapper objectMapper) {
        JsonNode root = JsonUtils.readJsonFileToNode(objectMapper, TIER_RANGE_PATH);
        Map<Integer, Map<TierEnum, TierRangeInfo>> map = new HashMap<>();

        for (JsonNode entry : root) {
            Map<TierEnum, TierRangeInfo> tierRangeMap = new EnumMap<>(TierEnum.class);
            tierRangeMap.put(TierEnum.UNRANK, TierRangeInfo.Unrank());

            for (TierEnum tier : TierEnum.values()) {
                JsonNode tierNode = entry.get(tier.name());
                if (tierNode == null) continue;
                TierRangeInfo info = objectMapper.treeToValue(tierNode, TierRangeInfo.class);
                tierRangeMap.put(tier, info.withTierEnum(tier));
            }

            // 같은 티어 구간을 공유하는 시즌 ID들을 모두 등록
            for (JsonNode seasonId : entry.get("SeasonId")) {
                map.put(seasonId.asInt(), tierRangeMap);
            }
        }

        return new TierRangeMeta(map);
    }
}
