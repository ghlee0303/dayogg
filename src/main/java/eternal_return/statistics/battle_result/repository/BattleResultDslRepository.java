package eternal_return.statistics.battle_result.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eternal_return.statistics.battle_result.BattleResult;
import eternal_return.statistics.battle_result.repository.condition.BattleResultRangeCondition;
import eternal_return.statistics.battle_result.repository.predicate.BattleResultPredicate;
import eternal_return.statistics.tier.enums.TierEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

import static eternal_return.statistics.battle_result.QBattleResult.battleResult;

@Repository
@RequiredArgsConstructor
public class BattleResultDslRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 탈퇴하지 않은 유저의 가장 최근 게임 ID를 반환한다.
     *
     * @param playerId 조회할 유저 ID
     * @return 가장 최근 게임의 gameId (전적이 없으면 null)
     */
    public Integer findLatestGameIdByPlayer(Long playerId) {
        return queryFactory.select(battleResult.gameId)
                .from(battleResult)
                .where(battleResult.playerId.eq(playerId))
                .orderBy(battleResult.startDtm.desc())
                .limit(1)
                .fetchOne();
    }

    public List<BattleResult> findByRange(BattleResultRangeCondition condition) {
        return queryFactory
                .selectFrom(battleResult)
                .where(BattleResultPredicate.fromRangeCondition(condition))
                .fetch();
    }

    /**
     * 범위 조건에 해당하는 전적을 최신순으로 offset 페이지네이션 조회한다.
     *
     * @param condition 조회 범위 조건
     * @param offset    건너뛸 행 수
     * @param limit     조회할 최대 행 수
     * @return 최신순({@code startDtm desc, id desc}) 전적 목록
     */
    public List<BattleResult> findPageByRange(BattleResultRangeCondition condition, long offset, int limit) {
        return queryFactory
                .selectFrom(battleResult)
                .where(BattleResultPredicate.fromRangeCondition(condition))
                .orderBy(battleResult.startDtm.desc(), battleResult.id.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    /**
     * 범위 조건에 해당하는 전적의 전체 개수를 조회한다.
     *
     * @param condition 조회 범위 조건
     * @return 전체 전적 수
     */
    public long countByRange(BattleResultRangeCondition condition) {
        Long count = queryFactory
                .select(battleResult.count())
                .from(battleResult)
                .where(BattleResultPredicate.fromRangeCondition(condition))
                .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 시즌 내에서 가장 많이 플레이한 캐릭터의 가장 최근 전적을 조회한다.
     *
     * <p>플레이 횟수가 같으면 더 최근에 플레이한 캐릭터를 우선한다.</p>
     *
     * @param playerId 조회할 유저 ID
     * @param seasonId 조회할 시즌 ID
     * @return 최다 플레이 캐릭터의 최근 {@link BattleResult} (전적이 없으면 empty)
     */
    public Optional<BattleResult> findLatestByMostPlayedCharacter(long playerId, int seasonId) {
        Integer characterNum = queryFactory
                .select(battleResult.characterNum)
                .from(battleResult)
                .where(
                        battleResult.playerId.eq(playerId),
                        battleResult.seasonId.eq(seasonId)
                )
                .groupBy(battleResult.characterNum)
                .orderBy(battleResult.count().desc(), battleResult.startDtm.max().desc())
                .limit(1)
                .fetchOne();

        if (characterNum == null) return Optional.empty();

        return Optional.ofNullable(queryFactory
                .selectFrom(battleResult)
                .where(
                        battleResult.playerId.eq(playerId),
                        battleResult.seasonId.eq(seasonId),
                        battleResult.characterNum.eq(characterNum)
                )
                .orderBy(battleResult.startDtm.desc(), battleResult.id.desc())
                .limit(1)
                .fetchOne());
    }

    /**
     * 범위 조건에 해당하는 전적에서 사용된 서로 다른 {@code tierEnum} 목록을 조회한다.
     *
     * @param condition 조회 범위 조건
     * @return 구별되는 tierEnum 목록 (2개 이상이면 서로 다른 티어가 섞여 있음)
     */
    public List<TierEnum> findDistinctTierEnums(BattleResultRangeCondition condition) {
        return queryFactory
                .select(battleResult.tierEnum)
                .distinct()
                .from(battleResult)
                .where(BattleResultPredicate.fromRangeCondition(condition))
                .fetch();
    }
}
