package eternal_return.statistics.core.exception;

import eternal_return.statistics.common.utils.MapUtils;
import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class BusinessException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorType;
    private final String responseMessage;
    private final String logMessage;
    private final Map<String, Object> context = new LinkedHashMap<>();

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

    public String toStringContext() {
        return !context.isEmpty()
                ? " | " + MapUtils.toString(context)
                : "";
    }
}
