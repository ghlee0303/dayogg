package eternal_return.dayogg.core.exception;

import eternal_return.dayogg.core.exception.enums.ExceptionResponseEnum;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorType;
    private final String responseMessage;
    private final String logMessage;
    private final Map<String, Object> context = new LinkedHashMap<>();

    /**
     * 하위 예외 클래스({@code SeasonException} 등)가 타는 생성자.
     *
     * <p>{@code super(logMessage)} 를 반드시 부른다 — 로깅 3곳({@code ControllerLoggingAspect}·
     * {@code ServiceLoggingAspect}·{@code StructuredLog})이 {@code error.message} 를
     * {@code getMessage()} 로 채우므로, 이걸 빠뜨리면 {@code logMessage} 를 넘겨도
     * 로그에는 {@code null} 이 찍힌다. Lombok {@code @RequiredArgsConstructor} 로 두면
     * 필드 대입만 하고 암묵적 {@code super()} 를 타서 정확히 그 일이 벌어진다.
     */
    public BusinessException(
            HttpStatus httpStatus, String errorType, String responseMessage, String logMessage
    ) {
        super(logMessage);
        this.httpStatus = httpStatus;
        this.errorType = errorType;
        this.responseMessage = responseMessage;
        this.logMessage = logMessage;
    }

    public BusinessException(ExceptionResponseEnum responseEnum, String logMessage) {
        super(logMessage);
        this.httpStatus = responseEnum.getStatus();
        this.errorType = responseEnum.name();
        this.responseMessage = responseEnum.getResponseMessage();
        this.logMessage = logMessage;
    }

    public BusinessException(ExceptionResponseEnum responseEnum, String errorType, String logMessage) {
        super(logMessage);
        this.httpStatus = responseEnum.getStatus();
        this.errorType = errorType;
        this.responseMessage = responseEnum.getResponseMessage();
        this.logMessage = logMessage;
    }

    public BusinessException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }
}
