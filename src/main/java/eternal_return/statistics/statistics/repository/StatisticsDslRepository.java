package eternal_return.statistics.statistics.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eternal_return.statistics.tier.enums.TierEnum;
import eternal_return.statistics.statistics.Statistics;
import eternal_return.statistics.statistics.condition.StatisticsUniqueCondition;
import eternal_return.statistics.statistics.predicate.StatisticsPredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static eternal_return.statistics.statistics.QStatistics.statistics;

@Component
@RequiredArgsConstructor
public class StatisticsDslRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 조건 컬럼: {@code user_id, character_num, tier_enum, season_id}
     *
     * @param condition 검색 조건
     * @return 조건에 일치하는 Statistics (없으면 {@link Optional#empty()})
     */
    public Optional<Statistics> findOneByMainCondition(StatisticsUniqueCondition condition) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(statistics)
                        .where(
                                StatisticsPredicate.essentialIdentifier(condition.playerId(), condition.seasonId()),
                                statistics.tierEnum.eq(condition.tierEnum()),
                                statistics.characterNum.eq(condition.characterNum()),
                                statistics.weaponNum.eq(condition.weaponNum())
                        )
                        .fetchOne()
        );
    }

    public List<Statistics> findSeasonTotal(Long playerId, Integer seasonId) {
        return queryFactory
                .selectFrom(statistics)
                .where(
                        StatisticsPredicate.essentialIdentifier(playerId, seasonId)
                )
                .fetch();
    }

    public List<Statistics> findByTierEnum(Long playerId, Integer seasonId, TierEnum tierEnum) {
        return queryFactory
                .selectFrom(statistics)
                .where(
                        StatisticsPredicate.essentialIdentifier(playerId, seasonId),
                        statistics.tierEnum.eq(tierEnum)
                )
                .fetch();
    }

    public List<Statistics> findByTierEnumList(
            Long playerId, Integer seasonId, List<TierEnum> tierEnumList
    ) {
        if (tierEnumList.isEmpty()) return List.of();

        return queryFactory
                .selectFrom(statistics)
                .where(
                        StatisticsPredicate.essentialIdentifier(playerId, seasonId),
                        statistics.tierEnum.in(tierEnumList)
                )
                .fetch();
    }
}
