package eternal_return.dayogg.route_auth.service;

import eternal_return.dayogg.route_auth.RouteAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RouteAuthScheduler {

    private final RouteAuthService routeAuthService;

    // route_auth 도메인 보류 — 스케줄러 비활성화 (재개하려면 @Scheduled 주석 해제)
    // @Scheduled(fixedRate = 60_000L)
    public void verifyPendingAuths() {
        for (RouteAuth auth : routeAuthService.findPendingAuthList()) {
            try {
                routeAuthService.verifyAuth(auth.getId());
            } catch (Exception e) {
                log.warn("[RouteAuthScheduler] verify failed authId={} | routeId={}", auth.getId(), auth.getRouteId(), e);
            }
        }
    }
}
