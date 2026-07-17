package eternal_return.statistics.core.annotation.service_logging;

import eternal_return.statistics.common.log.LogContext;
import eternal_return.statistics.core.annotation.SpelFacade;
import eternal_return.statistics.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * {@link ServiceLogging} 어노테이션이 붙은 Service 메서드를 감싸
 * 실행 시작·종료·오류를 로깅하는 AOP Aspect.
 *
 * <p>각 호출마다 6자리 랜덤 코드({@code code})를 생성하여
 * START → END(또는 ERROR) 로그를 연결할 수 있다.
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
            case PARENT -> log.info("[{}] | {}ms | {}", methodName, elapsed, LogContext.toStringAndClear());
            case CHILD -> LogContext.put(methodName, elapsed);
        }
    }

    private void logFailure(String methodName, ServiceLogging serviceLogging, Exception e) {
        log.error("[{}-MSG] | {}", methodName, e.getMessage());

        String ctx = drainLogContext(serviceLogging.loggingType()) + exceptionContext(e);
        if (!ctx.isEmpty()) {
            log.error("[{}-CTX] | {}", methodName, ctx);
        }
    }

    private String drainLogContext(LoggingType type) {
        return type == LoggingType.PARENT ? LogContext.toStringAndClear() : "";
    }

    private String exceptionContext(Exception e) {
        return (e instanceof BusinessException be) ? be.toStringContext() : "";
    }
}