package eternal_return.dayogg.route_auth;

import eternal_return.dayogg.route_auth.dto.RouteAuthResponse;
import eternal_return.dayogg.route_auth.service.RouteAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route_auth")
@RequiredArgsConstructor
public class RouteAuthController {
    private final RouteAuthService routeAuthService;

    @GetMapping("/recent")
    public ResponseEntity<RouteAuthResponse> getRecentAuth(
            @RequestParam Long playerId
    ) {
        return ResponseEntity.ok(
                routeAuthService.getRecentAuthResponse(playerId)
        );
    }

    @PostMapping
    public ResponseEntity<RouteAuthResponse> postSubmitAuth(
            @RequestParam String title,
            @RequestParam String routeId,
            @RequestParam Long playerId
    ) {
        RouteAuth newAuth = routeAuthService.createAuth(title, routeId, playerId);

        return ResponseEntity.ok(
                RouteAuthResponse.from(newAuth)
        );
    }

    @DeleteMapping
    public ResponseEntity<Boolean> deleteAuth(
            @RequestParam Long authId
    ) {
        routeAuthService.delete(authId);
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
