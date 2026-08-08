package eternal_return.dayogg.core.annotation.service_logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service 메서드에 붙이면 {@link ServiceLoggingAspect}가 실행 종료·오류를 자동으로 로깅한다.
 *
 * <p>핵심은 <b>호출 사슬 전체를 JSON 이벤트 한 개로 합치는 것</b>이다. 메서드마다 한 줄씩
 * 남기면 사슬이 깊어질수록 줄이 늘고 값이 흩어지는데, 안쪽 메서드는 자기 소요 시간을
 * {@code LogContext}(ThreadLocal)에 <b>적립만</b> 하고 가장 바깥 하나가 그걸 몰아서 flush 한다.
 * 그래서 {@code elapsedMs}·{@code apiMillis}·{@code playerId} 가 한 줄의 최상위 필드로 모인다.
 *
 * <pre>
 *            성공                                   실패
 * 최외곽     INFO 1줄 + LogContext 를 필드로 flush   ERROR 1줄 + flush
 * 안쪽       로그 없음, 소요 시간만 적립              발원지를 failedAt 에 남기고 전파
 * </pre>
 *
 * <p>실패는 예외 종류와 무관하게 ERROR 로 남기고 원본 예외를 그대로 전파한다.
 * 안쪽에서 터졌다면 어느 메서드였는지가 {@code failedAt} 필드로 그 한 줄에 실린다.
 *
 * <p>요청 단위 추적 코드({@code code})는 이 어노테이션이 만들지 않는다.
 * {@link eternal_return.dayogg.core.annotation.controller_logging.ControllerLogging} 이
 * MDC 에 넣은 값이 같은 스레드라 자동으로 따라붙는다.
 *
 * <p>사용 예 — 진입점이든 안쪽이든 똑같이 붙이기만 한다:
 * <pre>
 * {@literal @}ServiceLogging
 * public SseJobResult refresh(Long playerId) {      // 최외곽이라 여기서 한 줄이 나간다
 *     LogContext.put("playerId", playerId);         // 도메인 값도 같은 줄의 필드로 실린다
 *     ...
 * }
 *
 * {@literal @}ServiceLogging
 * public BattleResultApiResult fetchNewBattleResult(PlayerDto player) { ... }
 * // 위 한 줄에 "fetchNewBattleResult": 1840 으로 합류한다
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLogging {
}
