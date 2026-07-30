package eternal_return.statistics.common.log;

import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * AOP({@code @ControllerLogging}·{@code @ServiceLogging})가 감싸지 않는 지점에서
 * 구조화 로그를 남기기 위한 헬퍼. 값을 message 에 넣지 않고 필드로 남긴다.
 *
 * <pre>
 * StructuredLog.warn(log, "sse", e)
 *         .addKeyValue("sseKey", sseKey)
 *         .log("[SSE-SEND] send failed");
 * </pre>
 *
 * <p><b>주의</b> — 예외를 받는 메서드는 {@code error.type}·{@code error.message} 를 필드로 쓴다.
 * 여기에 {@code setCause(e)} 를 덧붙이면 인코더가 만드는 {@code error} 객체와 이름이 충돌해
 * <b>그 로그 이벤트가 통째로 버려진다.</b> 스택트레이스가 필요하면 {@code error.*} 를 쓰지 않고
 * {@code setCause} 만 쓴다.
 */
public final class StructuredLog {

    private static final String LAYER = "layer";
    private static final String ERROR_TYPE = "error.type";
    private static final String ERROR_MESSAGE = "error.message";

    private StructuredLog() {
    }

    /**
     * @param log   호출하는 클래스의 로거 — 이벤트의 {@code log.logger} 를 발생 지점으로 유지한다
     * @param layer 발생 계층 (예: {@code sse}, {@code api}, {@code redis})
     */
    public static LoggingEventBuilder info(Logger log, String layer) {
        return log.atInfo().addKeyValue(LAYER, layer);
    }

    public static LoggingEventBuilder warn(Logger log, String layer) {
        return log.atWarn().addKeyValue(LAYER, layer);
    }

    public static LoggingEventBuilder warn(Logger log, String layer, Throwable e) {
        return withError(log.atWarn().addKeyValue(LAYER, layer), e);
    }

    public static LoggingEventBuilder error(Logger log, String layer, Throwable e) {
        return withError(log.atError().addKeyValue(LAYER, layer), e);
    }

    private static LoggingEventBuilder withError(LoggingEventBuilder event, Throwable e) {
        return event.addKeyValue(ERROR_TYPE, e.getClass().getSimpleName())
                .addKeyValue(ERROR_MESSAGE, e.getMessage());
    }
}
