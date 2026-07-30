package eternal_return.statistics.core.annotation.controller_logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link ControllerLogging} 어노테이션이 붙은 Controller 메서드를 감싸
 * HTTP 요청의 종료·오류를 로깅하는 AOP Aspect.
 *
 * <p>각 요청마다 6자리 랜덤 코드({@code code})를 생성해 MDC 에 넣는다.
 * 같은 스레드에서 나오는 하위 서비스 로그에도 자동으로 붙어 요청 단위 추적 축이 된다.
 * 요청이 끝나면 진입 시점의 MDC 로 되돌린다.
 *
 * <p>클라이언트 IP는 리버스 프록시 헤더({@code X-Forwarded-For} 등)를 우선 확인하고,
 * 없으면 {@code remoteAddr}을 사용한다.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private static final String CODE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;

    private final HttpServletRequest request;

    /**
     * {@link ControllerLogging} 어노테이션이 붙은 메서드를 가로채 요청 로그를 기록한다.
     *
     * @param joinPoint         실제 메서드 실행 지점
     * @param controllerLogging 어노테이션 인스턴스 (태그 값 포함)
     * @return 원본 메서드의 반환값
     * @throws Throwable 원본 메서드에서 발생한 예외를 그대로 전파
     */
    @Around("@annotation(controllerLogging)")
    public Object logRequest(ProceedingJoinPoint joinPoint, ControllerLogging controllerLogging) throws Throwable {
        String ip = getClientIp();
        String url = request.getRequestURI();
        String method = request.getMethod();
        String tag = controllerLogging.value();

        // 같은 요청에서 나온 로그를 잇는 추적 축. MDC 에 넣으면 하위 서비스 로그까지 따라붙는다.
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
        MDC.put("code", generateCode());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            requestEvent(log.atInfo(), method, url, ip, tag)
                    .addKeyValue("elapsedMs", System.currentTimeMillis() - start)
                    .log("[{} {}] done", method, url);
            return result;

        } catch (RuntimeException e) {
            requestEvent(log.atError(), method, url, ip, tag)
                    .addKeyValue("error.type", e.getClass().getSimpleName())
                    .addKeyValue("error.message", e.getMessage())
                    .log("[{} {}] failed", method, url);
            throw e;

        } finally {
            // code 외에 요청 처리 중 추가된 키(idempotentKey 등)까지 한 번에 되돌린다.
            restoreMdc(mdcSnapshot);
        }
    }

    private void restoreMdc(Map<String, String> snapshot) {
        if (snapshot == null) {
            MDC.clear();
            return;
        }

        MDC.setContextMap(snapshot);
    }

    private LoggingEventBuilder requestEvent(
            LoggingEventBuilder event, String method, String url, String ip, String tag
    ) {
        return event.addKeyValue("layer", "controller")
                .addKeyValue("http.method", method)
                .addKeyValue("http.uri", url)
                .addKeyValue("client.ip", ip)
                .addKeyValue("tag", tag);
    }

    /** 요청 추적용 6자리 영숫자 코드. */
    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(ThreadLocalRandom.current().nextInt(CODE_CHARS.length())));
        }

        return sb.toString();
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