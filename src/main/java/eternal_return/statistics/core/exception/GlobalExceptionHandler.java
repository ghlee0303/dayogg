package eternal_return.statistics.core.exception;

import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 로그를 남기지 않는다 — {@code ServiceLoggingAspect} 가 이미 같은 값으로 남겼다.
     * {@link BusinessException} 생성자가 {@code super(logMessage)} 라
     * {@code getMessage()} 와 {@code getLogMessage()} 가 같은 값이고 {@code error.type} 도 동일해,
     * 여기서 한 줄 더 남기면 필드가 완전히 겹치는 중복이 된다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.fromBusiness(e));
    }

    /**
     * 어디서도 처리되지 않은 예외를 여기서 끝낸다.
     *
     * <p>이 핸들러가 없으면 예외가 Tomcat 까지 올라가
     * {@code org.apache.catalina.core.ContainerBase.[Tomcat]...[dispatcherServlet]} 이 ERROR 로
     * 스택트레이스를 통째로 찍는다. 한 번에 9KB 가 넘고, 앱이 이미 남긴 구조화 로그와 중복이다.
     * 여기서 잡으면 Tomcat 이 예외를 볼 일이 없어 그 덤프가 사라진다.
     *
     * <p>스택은 {@code setCause} 로만 싣는다 — {@code error.type}·{@code error.message} 를 같이 넣으면
     * 인코더가 만드는 {@code error} 객체와 이름이 충돌해 이벤트가 통째로 버려진다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception e, HttpServletRequest request
    ) throws Exception {
        // Spring MVC 가 스스로 4xx 로 옮기는 예외는 건드리지 않는다. 여기서 잡으면 전부 500 이 되어 버린다.
        // 이들은 DefaultHandlerExceptionResolver 가 처리하므로 Tomcat 까지 올라가지도 않는다.
        // 대부분은 ErrorResponse 를 구현하지만(405·415·406 등) 둘은 아니라서 따로 짚는다 —
        // 깨진 JSON(HttpMessageConversionException), 파라미터 타입 불일치(TypeMismatchException).
        if (isHandledBySpringMvc(e)) {
            throw e;
        }

        log.atError()
                .addKeyValue("layer", "exception")
                .addKeyValue("http.method", request.getMethod())
                .addKeyValue("http.uri", request.getRequestURI())
                .setCause(e)
                .log("[EXCEPTION] unexpected");

        return ResponseEntity.status(ExceptionResponseEnum.SERVER_ERROR.getStatus())
                .body(ErrorResponse.unexpected(ExceptionResponseEnum.SERVER_ERROR.getResponseMessage()));
    }

    private boolean isHandledBySpringMvc(Exception e) {
        return e instanceof org.springframework.web.ErrorResponse
                || e instanceof HttpMessageConversionException
                || e instanceof TypeMismatchException;
    }
}
