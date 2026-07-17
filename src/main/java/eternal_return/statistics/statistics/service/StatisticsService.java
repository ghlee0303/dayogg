package eternal_return.statistics.statistics.service;

import eternal_return.statistics.core.annotation.service_logging.ServiceLogging;
import eternal_return.statistics.player.dto.PlayerSeasonDto;
import eternal_return.statistics.player.service.PlayerService;
import eternal_return.statistics.statistics.dto.request.range.StatisticsRangeRequest;
import eternal_return.statistics.statistics.dto.response.StatisticsResponse;
import eternal_return.statistics.tier.enums.TierEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsAggregationService statisticsAggregationService;
    private final PlayerService playerService;

    @ServiceLogging
    public List<StatisticsResponse> getByRange(
            Long playerId, Integer seasonId, List<StatisticsRangeRequest> requestList
    ) {
        List<StatisticsResponse> responseDtoList = new ArrayList<>();

        for (StatisticsRangeRequest request : requestList) {
            StatisticsResponse aggregate = statisticsAggregationService.aggregateResponseByRange(
                    playerId, seasonId, request
            );

            responseDtoList.add(aggregate);
        }

        return responseDtoList;
    }

    @ServiceLogging
    public List<StatisticsResponse> getBySeasonTotal(Long playerId, Integer seasonId) {
        Map<TierEnum, StatisticsResponse.Tier> tierMap =
                statisticsAggregationService.aggregateTierResponseSeasonMap(playerId, seasonId);

        PlayerSeasonDto playerSeason = playerService.getPlayerSeason(playerId, seasonId);
        StatisticsResponse seasonTotal = new StatisticsResponse.SeasonTotal(playerSeason, tierMap);

        // TierEnum순으로 sort
        List<StatisticsResponse> responseDtoList = new ArrayList<>();
        for (TierEnum tierEnum : TierEnum.values()) {
            StatisticsResponse aggregate = tierMap.get(tierEnum);
            if (aggregate == null) continue;

            if (tierEnum.isRank()) {
                seasonTotal.merge(aggregate);
            }

            responseDtoList.add(aggregate);
        }
        responseDtoList.add(seasonTotal);

        StatisticsResponse recent = statisticsAggregationService.aggregateResponseByRecent(
                playerId, seasonId, "최근 일주일", 7
        );

        if (recent != null) {
            responseDtoList.add(recent);
        }

        return responseDtoList.reversed();
    }
}