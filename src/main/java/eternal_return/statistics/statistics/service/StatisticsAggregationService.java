package eternal_return.statistics.statistics.service;

import eternal_return.statistics.battle_result.BattleResult;
import eternal_return.statistics.battle_result.repository.BattleResultDslRepository;
import eternal_return.statistics.battle_result.repository.condition.BattleResultRangeCondition;
import eternal_return.statistics.core.annotation.service_logging.ServiceLogging;
import eternal_return.statistics.common.utils.EnumUtils;
import eternal_return.statistics.statistics.dto.request.range.StatisticsRequestSide;
import eternal_return.statistics.statistics.dto.request.range.StatisticsRangeRequest;
import eternal_return.statistics.statistics.dto.response.StatisticsResponse;
import eternal_return.statistics.statistics.Statistics;
import eternal_return.statistics.statistics.repository.StatisticsDslRepository;
import eternal_return.statistics.statistics.repository.StatisticsRepository;
import eternal_return.statistics.statistics.condition.StatisticsUniqueCondition;
import eternal_return.statistics.tier.enums.TierEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsAggregationService {
    private final BattleResultDslRepository battleResultDslRepository;
    private final StatisticsDslRepository statisticsDslRepository;

    @ServiceLogging
    @Transactional
    public StatisticsResponse aggregateResponseByRange(
            Long playerId, Integer seasonId, StatisticsRangeRequest request
    ) {
        StatisticsRequestSide start = request.start();
        StatisticsRequestSide end = request.end();

        List<TierEnum> tierList = EnumUtils.listBetween(TierEnum.class, start.tierEnum(), end.tierEnum());

        if (tierList.size() > 1) {
            return aggregateRangeResponse(playerId, seasonId, request, tierList);
        } else {
            return aggregateTierResponse(playerId, seasonId, request, start.tierEnum());
        }
    }

    private StatisticsResponse.Range aggregateRangeResponse(
            Long playerId, Integer seasonId, StatisticsRangeRequest request, List<TierEnum> tierEnumList
    ) {
        StatisticsResponse.Range response = new StatisticsResponse.Range(request);

        battleResultDslRepository.findByRange(
                BattleResultRangeCondition.of(
                        playerId, seasonId,
                        request.start().mmr(), request.end().mmr(),
                        tierEnumList,
                        request.start().date(), request.end().date()
                )
        ).forEach(response::merge);

        return response;
    }

    private StatisticsResponse.Tier aggregateTierResponse(
            Long playerId, Integer seasonId, StatisticsRangeRequest request, TierEnum tierEnum
    ) {
        StatisticsResponse.Tier response = new StatisticsResponse.Tier(request, tierEnum);

        battleResultDslRepository.findByRange(
                BattleResultRangeCondition.of(
                        playerId, seasonId,
                        tierEnum,
                        request.start().date(), request.end().date()
                )
        ).forEach(response::merge);

        return response;
    }

    @Transactional
    public Map<TierEnum, StatisticsResponse.Tier> aggregateTierResponseSeasonMap(
            Long playerId, Integer seasonId
    ) {
        Map<TierEnum, StatisticsResponse.Tier> tierMap = new EnumMap<>(TierEnum.class);
        List<BattleResult> battleResultList = battleResultDslRepository.findByRange(
                BattleResultRangeCondition.of(playerId, seasonId)
        );

        for (BattleResult battleResult : battleResultList) {
            tierMap.computeIfAbsent(
                    battleResult.getTierEnum(), StatisticsResponse.Tier::new
            ).merge(battleResult);
        }

        return tierMap;
    }

    @Transactional
    public StatisticsResponse aggregateResponseByRecent(
            Long playerId, Integer seasonId, String label, Integer recentDay
    ) {
        StatisticsResponse response;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(recentDay);
        BattleResultRangeCondition condition = BattleResultRangeCondition.of(
                playerId, seasonId,
                start, end
        );

        List<BattleResult> battleResultList = battleResultDslRepository.findByRange(condition);
        if (battleResultList.isEmpty()) return null;

        Set<TierEnum> tierEnumSet = battleResultList.stream()
                .map(BattleResult::getTierEnum)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TierEnum.class)));

        if (tierEnumSet.size() > 1) {
            response = new StatisticsResponse.Range(label);
        } else {
            response = new StatisticsResponse.Tier(label, tierEnumSet.iterator().next());
        }

        battleResultList.forEach(response::merge);

        return response;
    }

//    @Transactional
//    public void aggregateFromBattleResult(List<BattleResult> battleResultList) {
//        Map<StatisticsAggregationMapKey, Statistics> aggregationMap = new HashMap<>();
//
//        for (BattleResult battleResult : battleResultList) {
//            StatisticsAggregationMapKey key = StatisticsAggregationMapKey.fromBattleResult(battleResult);
//
//            aggregationMap.put(
//                    key,
//                    mergeFromRepository(aggregationMap.get(key), key.tierEnum(), battleResult)
//            );
//        }
//
//        statisticsRepository.saveAll(aggregationMap.values());
//    }

    private Statistics mergeFromRepository(
            Statistics statistics, TierEnum tierEnum, BattleResult battleResult
    ) {
        if (statistics == null) {
            statistics = statisticsDslRepository
                    .findOneByMainCondition(StatisticsUniqueCondition.fromBattleResult(battleResult))
                    .orElseGet(() -> new Statistics(battleResult, tierEnum));
        }
        statistics.merge(battleResult);

        return statistics;
    }

}
