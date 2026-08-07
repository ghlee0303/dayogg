package eternal_return.dayogg.route_auth.exception;

import eternal_return.dayogg.route_auth.enums.RouteAuthEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RouteApiException extends RuntimeException {
    private final RouteAuthEnum routeAuthEnum;

}
