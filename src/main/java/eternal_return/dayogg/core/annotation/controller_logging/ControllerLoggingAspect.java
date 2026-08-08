package eternal_return.dayogg.core.annotation.controller_logging;

import eternal_return.dayogg.common.log.TraceCode;
import eternal_return.dayogg.core.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * {@link ControllerLogging} 어노테이션이 붙은 Controller 메서드를 감싸
 * HTTP 요청의 종료·오류를 로깅하는 AOP Aspect.
 *
 * <p>각 요청마다 6자리 랜덤 코드({@code code})를 생성해 MDC 에 넣는다.
 * 같은 스레드에서 나오는 하위 서비스 로그에도 자동으로 붙어 요청 단위 추적 축이 된다.
 * 요청이 끝나면 진입 시점의 MDC 로 되돌린다.
 *
 * <p>레벨은 요청 결과가 정한다 — 예외·4xx/5xx·지연은 WARN/ERROR 로 올라가고,
 * 아무 문제 없이 끝난 요청만 {@link LoggingMode} 를 따른다({@code ALWAYS} → INFO,
 * {@code ON_ERROR} → DEBUG). 자세한 표는 {@link ControllerLogging} 참고.
 *
 * <p>클라이언트 IP는 리버스 프록시 헤더({@code X-Forwarded-For} 등)를 우선 확인하고,
 * 없으면 {@code remoteAddr}을 사용한다.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private final HttpServletRequest request;

    /**
     * {@link ControllerLogging} 어노테이션이 붙은 메서드를 가로채 요청 로그를 기록한다.
     *
     * @param joinPoint         실제 메서드 실행 지점
     * @param controllerLogging 어노테이션 인스턴스 (태그·모드 값 포함)
     * @return 원본 메서드의 반환값
     * @throws Throwable 원본 메서드에서 발생한 예외를 그대로 전파
     */
    @Around("@annotation(controllerLogging)")
    public Object logRequest(ProceedingJoinPoint joinPoint, ControllerLogging controllerLogging) throws Throwable {
        String url = request.getRequestURI();
        String method = request.getMethod();

        // 같은 요청에서 나온 로그를 잇는 추적 축. MDC 에 넣으면 하위 서비스 로그까지 따라붙는다.
        // ON_ERROR 라 이 Aspect 가 조용하더라도 하위 로그·예외 로그는 이 code 로 묶여야 하므로 항상 넣는다.
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
        MDC.put("code", TraceCode.generate());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            Level level = successLevel(controllerLogging, result, elapsed);
            // 레벨이 꺼져 있으면 IP 추출도 이벤트 조립도 하지 않는다 — ON_ERROR 의 평상시 비용이 0 이 되는 지점.
            if (log.isEnabledForLevel(level)) {
                requestEvent(log.atLevel(level), method, url, controllerLogging.value(), statusOf(result))
                        .addKeyValue("elapsedMs", elapsed)
                        .log("[{} {}] done", method, url);
            }
            return result;

        } catch (Exception e) {
            requestEvent(log.atLevel(failureLevel(e)), method, url, controllerLogging.value(), statusOf(e))
                    .addKeyValue("error.type", errorType(e))
                    .addKeyValue("error.message", e.getMessage())
                    .log("[{} {}] failed", method, url);
            throw e;

        } finally {
            // code 외에 요청 처리 중 추가된 키(idempotentKey 등)까지 한 번에 되돌린다.
            restoreMdc(mdcSnapshot);
        }
    }

    /**
     * 정상 반환된 요청의 레벨을 고른다.
     *
     * <p>상태 코드는 반환값에서 읽는다. {@code @Around} 는 {@code ResponseEntity} 가 응답에 반영되기
     * 전에 끝나므로 이 시점의 {@code response.getStatus()} 는 아직 200 이라 쓸 수 없다.
     */
    private Level successLevel(ControllerLogging controllerLogging, Object result, long elapsed) {
        Integer status = statusOf(result);
        if (status != null && status >= 400) {
            return status >= 500 ? Level.ERROR : Level.WARN;
        }
        if (controllerLogging.slowMs() > 0 && elapsed > controllerLogging.slowMs()) {
            return Level.WARN;
        }
        return controllerLogging.mode() == LoggingMode.ON_ERROR ? Level.DEBUG : Level.INFO;
    }

    /** 4xx 는 클라이언트가 요청을 잘못 보낸 것이지 서버 장애가 아니다 — ERROR 로 올리지 않는다. */
    private Level failureLevel(Exception e) {
        return (e instanceof BusinessException be && be.getHttpStatus().is4xxClientError())
                ? Level.WARN
                : Level.ERROR;
    }

    /**
     * 응답 상태. 알 수 없으면 null 이다 — {@code ResponseEntity} 가 아닌 반환값이나
     * 상태를 안 들고 있는 예외(런타임·checked)가 그렇다.
     */
    private Integer statusOf(Object outcome) {
        if (outcome instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        if (outcome instanceof BusinessException be) {
            return be.getHttpStatus().value();
        }
        return null;
    }

    /** {@code ServiceLoggingAspect} 와 같은 규칙 — 두 계층의 {@code error.type} 이 어긋나지 않게 한다. */
    private String errorType(Exception e) {
        return (e instanceof BusinessException be) ? be.getErrorType() : e.getClass().getSimpleName();
    }

    private void restoreMdc(Map<String, String> snapshot) {
        if (snapshot == null) {
            MDC.clear();
            return;
        }

        MDC.setContextMap(snapshot);
    }

    private LoggingEventBuilder requestEvent(
            LoggingEventBuilder event, String method, String url, String tag, Integer status
    ) {
        LoggingEventBuilder built = event.addKeyValue("layer", "controller")
                .addKeyValue("http.method", method)
                .addKeyValue("http.uri", url)
                .addKeyValue("client.ip", getClientIp())
                .addKeyValue("tag", tag);

        // 모르는 상태를 null 필드로 내보내면 수집만 늘고 질의에는 안 쓰인다.
        return (status == null) ? built : built.addKeyValue("http.status", status);
    }

    /**
     * 리버스 프록시 환경을 고려하여 실제 클라이언트 IP를 추출한다.
     * {@code X-Forwarded-For} 등 프록시 헤더를 우선 확인하고, 없으면 {@code remoteAddr}을 반환한다.
     */
    private String getClientIp() {
        String[] proxyHeaders = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP"};

        for (String header : proxyHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim(); // 다중 프록시 시 첫 번째 IP가 원본
            }
        }
        return request.getRemoteAddr();
    }
}
