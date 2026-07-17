package eternal_return.statistics.core.sse.exception;

import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import eternal_return.statistics.core.sse.exception.enums.SseJobErrorStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SseJobException extends RuntimeException {
    private SseJobErrorStatus errorStatus;
    private String responseMessage;
    private String logMessage;
    private Throwable throwable;

    public SseJobException(ExceptionResponseEnum exceptionResponseEnum, String logMessage) {
        super(logMessage);
        this.errorStatus = SseJobErrorStatus.ERROR;
        this.responseMessage = exceptionResponseEnum.getResponseMessage();
        this.logMessage = logMessage;
    }

    public SseJobException(ExceptionResponseEnum exceptionResponseEnum, Throwable throwable) {
        super(throwable);
        this.errorStatus = SseJobErrorStatus.ERROR;
        this.responseMessage = exceptionResponseEnum.getResponseMessage();
        this.logMessage = throwable.getMessage();
        this.throwable = throwable;
    }
}
