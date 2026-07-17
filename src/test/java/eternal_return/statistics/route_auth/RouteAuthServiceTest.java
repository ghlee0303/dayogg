package eternal_return.statistics.route_auth;

import eternal_return.statistics.route_auth.service.RouteAuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class RouteAuthServiceTest {
    private static final int VERIFY_LOOP_COUNT = 10;
    private static final long VERIFY_LOOP_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    @Autowired
    RouteAuthService routeAuthService;

    @Test
    void createAuth() {
    }

    @Test
    void verifyLoop() throws InterruptedException {
        routeAuthService.createAuth("987654", "9097", 1L);

        for (int i = 0; i < VERIFY_LOOP_COUNT; i++) {
            if (i > 0) Thread.sleep(VERIFY_LOOP_INTERVAL_MS);

            List<RouteAuth> list = routeAuthService.findPendingAuthList();
            for (RouteAuth auth : list) {
                log.info("{}번: {} | {}", i, auth.getRouteId(), auth.getTitle());
                routeAuthService.verifyAuth(auth.getId());
            }
        }
    }
}
