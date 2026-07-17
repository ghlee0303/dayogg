package eternal_return.statistics.route_auth.service;

import eternal_return.statistics.core.exception.BusinessException;
import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import eternal_return.statistics.core.exception.enums.LogMessageEnum;
import eternal_return.statistics.player.dto.PlayerDto;
import eternal_return.statistics.player.service.PlayerService;
import eternal_return.statistics.route_auth.RouteAuth;
import eternal_return.statistics.route_auth.client.RouteApiResponse;
import eternal_return.statistics.route_auth.dto.RouteAuthResponse;
import eternal_return.statistics.route_auth.repository.RouteAuthDslRepository;
import eternal_return.statistics.route_auth.repository.RouteAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteAuthService {

    private static final int CREATE_DUPLICATE_MINUTES = 20;
    static final int VERIFY_CREATE_MIN_MINUTES = 3;
    static final int VERIFY_CREATE_MAX_MINUTES = 10;
    static final int VERIFY_UPDATE_BEFORE_MINUTES = 1;

    private final RouteAuthRepository routeAuthRepository;
    private final RouteAuthDslRepository routeAuthDslRepository;
    private final RouteApiService routeApiService;
    private final PlayerService playerService;

    public RouteAuthResponse getRecentAuthResponse(Long playerId) {
        Optional<RouteAuth> optional = routeAuthDslRepository.findRecentByPlayerId(playerId);

        return optional.map(RouteAuthResponse::from)
                .orElse(RouteAuthResponse.empty());
    }

    @Transactional
    public RouteAuth createAuth(String title, String routeId, Long playerId) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(CREATE_DUPLICATE_MINUTES);
        if (routeAuthDslRepository.existsRecent(routeId, playerId, since)) {
            throw new BusinessException(
                    ExceptionResponseEnum.ROUTE_AUTH_DUPLICATE,
                    LogMessageEnum.DUPLICATE_ROUTE_AUTH.format(routeId, playerId)
            );
        }

        PlayerDto player = playerService.getPlayerDto(playerId);

        return routeAuthRepository.save(
                new RouteAuth(routeId, playerId, title, player.name())
        );
    }

    public List<RouteAuth> findPendingAuthList() {
        LocalDateTime now = LocalDateTime.now();
        return routeAuthDslRepository.findPendingForVerification(
                now.minusMinutes(VERIFY_CREATE_MAX_MINUTES),
                now.minusMinutes(VERIFY_CREATE_MIN_MINUTES),
                now.minusMinutes(VERIFY_UPDATE_BEFORE_MINUTES)
        );
    }

    @Transactional
    public void verifyAuth(Long authId) {
        RouteAuth auth = routeAuthRepository.findById(authId).orElse(null);
        if (auth == null) return;

        Optional<RouteApiResponse> optional = routeApiService.requestRoute(auth.getRouteId());
        if (optional.isEmpty()) {
            auth.touch();
            return;
        }

        RouteApiResponse response = optional.get();
        log.info("auth  : {} | {}", auth.getRouteId(), auth.getTitle());
        log.info("api   : {} | {}", response.getUserNickName(), response.getTitle());

        if (auth.authentication(response, VERIFY_CREATE_MAX_MINUTES)) {
            playerService.deletePlayer(auth.getPlayerId());
        }
        // 실패 시: authentication() 안에서 status 설정 → dirty checking으로 반영
    }

    @Transactional
    public void delete(Long authId) {
        routeAuthRepository.deleteById(authId);
    }
}
