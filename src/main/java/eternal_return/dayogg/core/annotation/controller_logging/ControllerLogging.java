package eternal_return.dayogg.core.annotation.controller_logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 메서드에 붙이면 {@link ControllerLoggingAspect}가
 * 요청 시작·종료·오류를 자동으로 로깅한다.
 *
 * <p>JSON 이벤트 한 개로 나가며 {@code code}·{@code http.method}·{@code http.uri}·
 * {@code client.ip}·{@code tag}·{@code elapsedMs} 가 최상위 필드로 붙는다.
 * 실패 시 {@code error.type}·{@code error.message} 가, 상태를 알 수 있으면
 * {@code http.status} 가 추가된다.
 *
 * <p>레벨은 요청 결과로 정해진다. {@link #mode()} 가 바꾸는 것은 마지막 줄뿐이다.
 * <pre>
 * 예외 — BusinessException 4xx        WARN
 * 예외 — 그 외                         ERROR
 * 정상 반환인데 status 가 4xx / 5xx     WARN / ERROR
 * 정상인데 elapsedMs > slowMs          WARN
 * 그 외 정상                           ALWAYS → INFO / ON_ERROR → DEBUG
 * </pre>
 *
 * <p>사용 예:
 * <pre>
 * {@literal @}ControllerLogging("user-search")
 * public ResponseEntity&lt;?&gt;search(...) { ... }
 *
 * {@literal @}ControllerLogging(mode = LoggingMode.ON_ERROR)
 * public ResponseEntity&lt;?&gt;meta(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerLogging {
    /** 로그에 함께 출력할 태그 (생략 가능) */
    String value() default "";

    /** 정상 요청을 남길지 여부. 기본은 남긴다. */
    LoggingMode mode() default LoggingMode.ALWAYS;

    /** 이 시간을 넘긴 정상 요청은 WARN 으로 올린다. 0 이하면 지연 판정을 끈다. */
    long slowMs() default 10000;
}
