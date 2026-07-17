package eternal_return.statistics.route_auth.dto;

import eternal_return.statistics.route_auth.RouteAuth;
import eternal_return.statistics.route_auth.enums.RouteAuthEnum;

import java.time.LocalDateTime;

public record RouteAuthResponse(
        Long id, String routeId, String title, RouteAuthEnum status,
        LocalDateTime createAt, LocalDateTime updateAt
) {
    public static RouteAuthResponse from(RouteAuth routeAuth) {
        return new RouteAuthResponse(
                routeAuth.getId(),
                routeAuth.getRouteId(),
                routeAuth.getTitle(),
                routeAuth.getStatus(),
                routeAuth.getCreateAt(),
                routeAuth.getUpdateAt()
        );
    }

    public static RouteAuthResponse empty() {
        return new RouteAuthResponse(
                null, null, null, null, null, null
        );
    }
}
