package eternal_return.statistics.core.annotation.controller_logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 메서드에 붙이면 {@link ControllerLoggingAspect}가
 * 요청 시작·종료·오류를 자동으로 로깅한다.
 *
 * <p>로그 형식:
 * <pre>
 * [C_START] [코드] METHOD URI | IP: x.x.x.x | tag: {value}
 * [C_END]   [코드] METHOD URI | Nms
 * [C_ERROR] [코드] URI | IP: x.x.x.x | 메시지
 * </pre>
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