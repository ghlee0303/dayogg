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
 * 실패 시 {@code error.type}·{@code error.message} 가 추가된다.
 *
 * <p>사용 예:
 * <pre>
 * {@literal @}ControllerLogging("user-search")
 * public ResponseEntity<?> search(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerLogging {
    /** 로그에 함께 출력할 태그 (생략 가능) */
    String value() default "";
}