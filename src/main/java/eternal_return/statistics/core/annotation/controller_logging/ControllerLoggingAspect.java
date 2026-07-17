package eternal_return.statistics.core.annotation.controller_logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link ControllerLogging} 어노테이션이 붙은 Controller 메서드를 감싸
 * HTTP 요청의 시작·종료·오류를 로깅하는 AOP Aspect.
 *
 * <p>각 요청마다 6자리 랜덤 코드({@code code})를 생성하여
 * START → END(또는 ERROR) 로그를 연결할 수 있다.
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
        // 동일 요청의 START·END 로그를 연결하기 위한 6자리 랜덤 코드

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            log.info("[{}_{}] | IP: {} | tag: {} | {}ms", method, url, ip, tag, elapsed);
            return result;

        } catch (RuntimeException e) {
            log.error("[{}_{}] | IP: {} | {}", method, url, ip, e.getMessage());
            throw e;
        }
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