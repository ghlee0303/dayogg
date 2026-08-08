package eternal_return.dayogg.core.annotation.service_logging;

import eternal_return.dayogg.common.log.LogContext;
import eternal_return.dayogg.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * {@link ServiceLogging} 어노테이션이 붙은 Service 메서드를 감싸
 * 실행 종료·오류를 로깅하는 AOP Aspect.
 *
 * <p>호출 사슬 전체가 JSON 이벤트 <b>한 개</b>로 나간다. 한 줄을 낼 프레임은 선언이 아니라
 * 구조가 정한다 — 스레드에서 {@link ServiceLogging} 에 <b>가장 먼저 진입한 프레임</b>이
 * 리턴할 때 {@link LogContext} 를 flush 하고 로그를 남긴다. 안쪽 프레임은 자기 소요 시간만
 * 적립한다. 그래서 {@code elapsedMs}·{@code apiMillis}·{@code playerId} 가 한 줄의 최상위 필드로 모인다.
 *
 * <p>요청 단위 추적 코드({@code code})는 이 Aspect 가 만들지 않는다.
 * {@link eternal_return.dayogg.core.annotation.controller_logging.ControllerLoggingAspect}
 * 가 MDC 에 넣은 값이 같은 스레드라 자동으로 따라붙는다.
 *
 * <p>실패는 예외 종류와 무관하게 ERROR 로 남기고 원본 예외를 그대로 전파한다.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceLoggingAspect {
    /** 실패 발원지를 가리키는 필드. 최외곽 줄만 남으므로 어느 안쪽 메서드가 던졌는지 이걸로 안다. */
    private static final String FAILED_AT = "failedAt";

    /**
     * {@link ServiceLogging}
     *
     * @param joinPoint      실제 메서드 실행 지점
     * @param serviceLogging 어노테이션 인스턴스
     * @return 원본 메서드의 반환값
     * @throws Throwable 원본 메서드에서 발생한 예외를 그대로 전파
     */
    @Around("@annotation(serviceLogging)")
    public Object logRequest(ProceedingJoinPoint joinPoint, ServiceLogging serviceLogging) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        boolean outermost = LogContext.enter();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            if (outermost) logSuccess(methodName, elapsed);
            else LogContext.put(methodName, elapsed);

            return result;

        } catch (Exception e) {
            // 안쪽은 발원지만 남기고 넘긴다. 줄은 최외곽에서 하나만 나간다.
            if (outermost) logFailure(methodName, e);
            else LogContext.putIfAbsent(FAILED_AT, methodName);

            throw e;

        } finally {
            LogContext.exit();
        }
    }

    private void logSuccess(String methodName, long elapsed) {
        LoggingEventBuilder event = log.atInfo()
                .addKeyValue("layer", "service")
                .addKeyValue("method", methodName)
                .addKeyValue("elapsedMs", elapsed);

        LogContext.drainMap().forEach(event::addKeyValue);
        event.log("[{}] done", methodName);
    }

    private void logFailure(String methodName, Exception e) {
        LoggingEventBuilder event = log.atError()
                .addKeyValue("layer", "service")
                .addKeyValue("method", methodName)
                .addKeyValue("error.type", errorType(e))
                .addKeyValue("error.message", e.getMessage());

        // 실패해도 지금까지 쌓인 값은 이 줄에 실어 보낸다 — 최외곽이 안 비우면 어디에도 안 남는다.
        // 두 출처를 맵에서 먼저 합친다: 같은 키를 addKeyValue 로 두 번 넣으면 인코더가
        // 이벤트를 통째로 버린다. playerId 처럼 LogContext 와 예외 컨텍스트에 함께 담기는 키가 실제로 있다.
        Map<String, Object> fields = LogContext.drainMap();
        if (e instanceof BusinessException be) {
            fields.putAll(be.getContext());
        }
        fields.forEach(event::addKeyValue);

        event.log("[{}] failed", methodName);
    }

    private String errorType(Exception e) {
        return (e instanceof BusinessException be) ? be.getErrorType() : e.getClass().getSimpleName();
    }
}
