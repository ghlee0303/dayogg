package eternal_return.dayogg.battle_result.service;

import eternal_return.dayogg.battle_result.BattleResult;
import eternal_return.dayogg.battle_result.BattleResultRepository;
import eternal_return.dayogg.battle_result.client.BattleResultApiResponse;
import eternal_return.dayogg.battle_result.dto.BattleResultApiResult;
import eternal_return.dayogg.battle_result.dto.BattlePageResult;
import eternal_return.dayogg.battle_result.dto.BattleRangeResult;
import eternal_return.dayogg.battle_result.dto.BattleResponse;
import eternal_return.dayogg.battle_result.dto.request.range.BattleRangeRequest;
import eternal_return.dayogg.battle_result.dto.request.range.BattleRequestSide;
import eternal_return.dayogg.battle_result.mapper.BattleResultMapper;
import eternal_return.dayogg.battle_result.repository.BattleResultDslRepository;
import eternal_return.dayogg.battle_result.repository.condition.BattleResultRangeCondition;
import eternal_return.dayogg.common.utils.EnumUtils;
import eternal_return.dayogg.tier.enums.TierEnum;
import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.player.dto.PlayerDto;
import eternal_return.dayogg.tier.TierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleResultService {

    private static final int MAX_PAGE_SIZE = 100;

    private final BattleResultRepository battleResultRepository;
    private final BattleResultDslRepository battleResultDslRepository;
    private final BattleResultApiService battleResultApiService;
    private final BattleResultMapper battleResultMapper;
    private final TierService tierService;

    @ServiceLogging
    public BattleResultApiResult fetchNewBattleResult(PlayerDto player) {
        Integer lastBattleResultId = battleResultDslRepository.findLatestGameIdByPlayer(player.id());

        return battleResultApiService.fetchGame(player, lastBattleResultId);
    }

    @ServiceLogging
    public List<BattleRangeResult> getByRange(
            Long playerId, Integer seasonId, List<BattleRangeRequest> requestList
    ) {
        List<BattleRangeResult> resultList = new ArrayList<>();

        for (BattleRangeRequest request : requestList) {
            resultList.add(aggregateResponseByRange(playerId, seasonId, request));
        }

        return resultList;
    }

    private BattleRangeResult aggregateResponseByRange(
            Long playerId, Integer seasonId, BattleRangeRequest request
    ) {
        BattleResultRangeCondition condition = toRangeCondition(playerId, seasonId, request);

        List<BattleResponse> games = battleResultDslRepository.findByRange(condition).stream()
                .map(battleResultMapper::toResponse)
                .toList();

        return new BattleRangeResult(request, games);
    }

    @ServiceLogging
    public BattlePageResult getPageByRange(
            Long playerId, Integer seasonId, BattleRangeRequest request, int page, int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);

        BattleResultRangeCondition condition = toRangeCondition(playerId, seasonId, request);

        long totalElements = battleResultDslRepository.countByRange(condition);

        List<BattleResult> br = battleResultDslRepository
                .findPageByRange(condition, (long) safePage * safeSize, safeSize);

        List<BattleResponse> games = br.stream()
                .map(battleResultMapper::toResponse)
                .toList();

        return BattlePageResult.of(games, safePage, safeSize, totalElements);
    }

    private BattleResultRangeCondition toRangeCondition(
            Long playerId, Integer seasonId, BattleRangeRequest request
    ) {
        BattleRequestSide start = request.start();
        BattleRequestSide end = request.end();

        List<TierEnum> tierList = EnumUtils.listBetween(TierEnum.class, start.tierEnum(), end.tierEnum());

        return (tierList.size() > 1)
                ? BattleResultRangeCondition.of(
                        playerId, seasonId,
                        start.mmr(), end.mmr(),
                        tierList,
                        start.date(), end.date())
                : BattleResultRangeCondition.of(
                        playerId, seasonId,
                        start.tierEnum(),
                        start.date(), end.date());
    }

    @Transactional
    public void saveBattleResult(Long playerId, List<BattleResultApiResponse> responseList) {
        if (responseList == null || responseList.isEmpty()) return;

        List<BattleResult> battleResultList = new ArrayList<>();

        for (BattleResultApiResponse response : responseList) {
            TierEnum tierEnum = tierService.resolveTier(response.getEndDtm(), response.getMmrBefore());
            BattleResult battleResult = battleResultMapper.toEntity(response, playerId, tierEnum);
            battleResultList.add(battleResult);
        }
        battleResultRepository.saveAll(battleResultList);
    }


}