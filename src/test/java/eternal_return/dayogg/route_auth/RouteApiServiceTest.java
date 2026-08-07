package eternal_return.dayogg.route_auth;

import eternal_return.dayogg.route_auth.client.RouteApiResponse;
import eternal_return.dayogg.route_auth.service.RouteApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RouteApiServiceTest {
    @Autowired
    private RouteApiService routeApiService;

    @Test
    void test() {
        String id = "66339";

//        RouteApiResponse d = routeApiService.requestRoute(id);
    }
}
