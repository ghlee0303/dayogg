package eternal_return.statistics.tier;

import eternal_return.statistics.tier.enums.TierEnum;
import eternal_return.statistics.meta.meta.season.SeasonInfo;
import eternal_return.statistics.meta.meta.season.SeasonMeta;
import eternal_return.statistics.core.annotation.service_logging.ServiceLogging;
import eternal_return.statistics.tier.dto.TierRangeResult;
import eternal_return.statistics.meta.meta.tier_range.TierRangeInfo;
import eternal_return.statistics.meta.meta.tier_range.TierRangeMeta;
import eternal_return.statistics.tier.exception.TierException;
import eternal_return.statistics.tier.top_tier_cut.TopTierCut;
import eternal_return.statistics.tier.top_tier_cut.service.TopTierCutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TierService {

    private final TopTierCutService topTierCutService;
    private final SeasonMeta seasonMeta;
    private final TierRangeMeta tierRangeMeta;

    @ServiceLogging
    public TierEnum resolveTier(LocalDateTime playDateTime, Integer mmr) {
        SeasonInfo seasonInfo = seasonMeta.getByDateTimeRange(playDateTime);

        Map<TierEnum, TierRangeInfo> resultMap = getTierRangeMap(playDateTime);

        try {
            return TierEnum.calculate(resultMap, mmr);
        } catch (TierException e) {
            throw e.withContext("seasonId", seasonInfo.seasonId())
                    .withContext("mmr", mmr)
                    .withContext("playDateTime", playDateTime);
        }
    }

    @ServiceLogging
    public TierEnum resolveTierWithFallback(
            @Nullable LocalDate playDateTime, Integer mmr, LocalDateTime fallbackDateTime
    ) {
        if (playDateTime != null) {
            return resolveTier(playDateTime.atStartOfDay(), mmr);
        }
        return resolveTier(fallbackDateTime, mmr);
    }

    @ServiceLogging
    public TierEnum resolveTier(Integer seasonId, Integer mmr) {
        SeasonInfo seasonInfo = seasonMeta.getById(seasonId);

        LocalDateTime playDateTime = seasonInfo.isCurrent()
                ? LocalDateTime.now() : seasonInfo.endDate().minusMinutes(20);

        return resolveTier(playDateTime, mmr);
    }

    @ServiceLogging
    public TierRangeInfo getTierRangeMeta(Integer seasonId, TierEnum tierEnum) {
        Map<TierEnum, TierRangeInfo> seasonTierMetaMap = getTierRangeMap(seasonId);
        TierRangeInfo meta = seasonTierMetaMap.get(tierEnum);

        if (meta == null) {
            throw new TierException.RangeOut("TierRangeInfo not found")
                    .withContext("seasonId", seasonId)
                    .withContext("tierEnum", tierEnum);
        }

        return meta;
    }

    @ServiceLogging
    public TierRangeInfo getTierRangeMeta(LocalDateTime playDateTime, TierEnum tierEnum) {
        Map<TierEnum, TierRangeInfo> seasonTierMetaMap = getTierRangeMap(playDateTime);

        TierRangeInfo meta = seasonTierMetaMap.get(tierEnum);

        if (meta == null) {
            throw new TierException.RangeOut("TierRangeInfo not found")
                    .withContext("playDateTime", playDateTime)
                    .withContext("tierEnum", tierEnum);
        }

        return meta;
    }

    @ServiceLogging
    public TierRangeResult getTierRangeByMmr(Integer seasonId, Integer startMmr, Integer endMmr) {
        Map<TierEnum, TierRangeInfo> seasonTierMetaMap = getTierRangeMap(seasonId);
        List<TierEnum> targetEnumList = TierEnum.calculateByRange(seasonTierMetaMap, startMmr, endMmr);

        if (targetEnumList.isEmpty()) {
            throw new TierException.RangeOut("targetEnumList is empty")
                    .withContext("seasonId", seasonId)
                    .withContext("startMmr", startMmr)
                    .withContext("endMmr", endMmr);
        }

        Map<TierEnum, TierRangeInfo> result = new EnumMap<>(TierEnum.class);
        for (TierEnum targetEnum : targetEnumList) {
            result.put(targetEnum, seasonTierMetaMap.get(targetEnum));
        }

        return new TierRangeResult(result);
    }

    @ServiceLogging
    public TierRangeResult getTierRangeByDate(
            Integer seasonId,
            @Nullable LocalDateTime dateTime
    ) {
        Map<TierEnum, TierRangeInfo> seasonTierMetaMap = dateTime != null
                ? getTierRangeMap(dateTime)
                : getTierRangeMap(seasonId);

        return new TierRangeResult(seasonTierMetaMap);
    }

    private Map<TierEnum, TierRangeInfo> getTierRangeMap(Integer seasonId) {
        TopTierCut topTierCut = topTierCutService.getDefaultTopTierCut(seasonId);

        return addTopTierRange(seasonId, topTierCut);
    }

    private Map<TierEnum, TierRangeInfo> getTierRangeMap(LocalDateTime dateTime) {
        SeasonInfo seasonInfo = seasonMeta.getByDateTimeRange(dateTime);
        TopTierCut topTierCut = topTierCutService.getTopTierCut(seasonInfo.seasonId(), dateTime);

        return addTopTierRange(seasonInfo.seasonId(), topTierCut);
    }

    private Map<TierEnum, TierRangeInfo> addTopTierRange(
            Integer seasonId,
            TopTierCut topTierCut
    ) {
        Map<TierEnum, TierRangeInfo> result = new EnumMap<>(tierRangeMeta.getBySeasonId(seasonId));

        // MITHRIL 상한 = DEMIGOD 커트 - 1 (step/gap/start는 원본 보존)
        result.put(TierEnum.MITHRIL,
                result.get(TierEnum.MITHRIL).withEnd(topTierCut.getDemiGodCut() - 1));

        // DEMIGOD: DemiGodCut ~ EternityCut-1
        result.put(TierEnum.DEMIGOD,
                new TierRangeInfo(TierEnum.DEMIGOD, topTierCut.getDemiGodCut(), topTierCut.getEternityCut() - 1));

        // ETERNITY: EternityCut ~ 상한 없음(999999)
        result.put(TierEnum.ETERNITY,
                new TierRangeInfo(TierEnum.ETERNITY, topTierCut.getEternityCut(), 999999));

        return result;
    }
}