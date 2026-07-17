package eternal_return.statistics.route_auth.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import eternal_return.statistics.route_auth.RouteAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static eternal_return.statistics.route_auth.QRouteAuth.routeAuth;

@Repository
@RequiredArgsConstructor
public class RouteAuthDslRepository {
    private final JPAQueryFactory queryFactory;

    public boolean existsRecent(String routeId, Long playerId, LocalDateTime since) {
        Integer hit = queryFactory
                .selectOne()
                .from(routeAuth)
                .where(
                        routeAuth.routeId.eq(routeId),
                        routeAuth.playerId.eq(playerId),
                        routeAuth.createAt.goe(since)
                )
                .fetchFirst();
        return hit != null;
    }

    public Optional<RouteAuth> findRecentByPlayerId(Long playerId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(routeAuth)
                .where(
                        routeAuth.playerId.eq(playerId)
                )
                .orderBy(routeAuth.createAt.desc())
                .fetchFirst()
        );
    }

    public List<RouteAuth> findPendingForVerification(
            LocalDateTime createAtFrom,
            LocalDateTime createAtTo,
            LocalDateTime updateAtBefore
    ) {
        return queryFactory
                .selectFrom(routeAuth)
                .where(
                        routeAuth.status.isNull(),
                        routeAuth.createAt.between(createAtFrom, createAtTo),
                        routeAuth.updateAt.loe(updateAtBefore)
                )
                .fetch();
    }
}
