package eternal_return.statistics.core.annotation.service_logging;

import eternal_return.statistics.common.log.LogContext;
import eternal_return.statistics.core.annotation.SpelFacade;
import eternal_return.statistics.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.stereotype.Component;

/**
 * {@link ServiceLogging} 어노테이션이 붙은 Service 메서드를 감싸
 * 실행 종료·오류를 로깅하는 AOP Aspect.
 *
 * <p>로그는 JSON 이벤트 한 개로 나가며, {@link LogContext} 에 쌓인 값과
 * {@code elapsedMs} 를 최상위 필드로 승격시킨다. 요청 단위 추적 코드({@code code})는
 * {@link eternal_return.statistics.core.annotation.controller_logging.ControllerLoggingAspect}
 * 가 MDC 에 넣은 값이 자동으로 따라붙는다.
 *
 * <p>예외 종류와 무관하게 모든 오류를 로깅한 뒤 원본 예외를 그대로 전파한다.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceLoggingAspect {
    /**
     * {@link ServiceLogging}
     *
     * @param joinPoint      실제 메서드 실행 지점
     * @param serviceLogging 어노테이션 인스턴스 (태그 값 포함)
     * @return 원본 메서드의 반환값
     * @throws Throwable 원본 메서드에서 발생한 예외를 그대로 전파
     */
    @Around("@annotation(serviceLogging)")
    public Object logRequest(ProceedingJoinPoint joinPoint, ServiceLogging serviceLogging) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            logSuccess(methodName, serviceLogging, System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            logFailure(methodName, serviceLogging, e);
            throw e;
        }
    }

    private void logSuccess(String methodName, ServiceLogging serviceLogging, long elapsed) {
        switch (serviceLogging.loggingType()) {
            case PARENT -> {
                LoggingEventBuilder event = log.atInfo()
                        .addKeyValue("layer", "service")
                        .addKeyValue("method", methodName)
                        .addKeyValue("elapsedMs", elapsed);

                LogContext.drainMap().forEach(event::addKeyValue);
                event.log("[{}] done", methodName);
            }
            case CHILD -> LogContext.put(methodName, elapsed);
        }
    }

    private void logFailure(String methodName, ServiceLogging serviceLogging, Exception e) {
        LoggingEventBuilder event = log.atError()
                .addKeyValue("layer", "service")
                .addKeyValue("method", methodName)
                .addKeyValue("error.type", errorType(e))
                .addKeyValue("error.message", e.getMessage());

        // CHILD 는 비우지 않는다. 바깥 PARENT 가 flush 할 때 함께 필드로 나가야 한다.
        if (serviceLogging.loggingType() == LoggingType.PARENT) {
            LogContext.drainMap().forEach(event::addKeyValue);
        }
        if (e instanceof BusinessException be) {
            be.getContext().forEach(event::addKeyValue);
        }

        event.log("[{}] failed", methodName);
    }

    private String errorType(Exception e) {
        return (e instanceof BusinessException be) ? be.getErrorType() : e.getClass().getSimpleName();
    }
}