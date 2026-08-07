package eternal_return.dayogg.core.exception.child;

import eternal_return.dayogg.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SeasonException extends BusinessException {
    public SeasonException(
            HttpStatus httpStatus,
            String errorType,
            String responseMessage,
            String logMessage
    ) {
        super(httpStatus, errorType, responseMessage, logMessage);
    }

    public static class NotFound extends SeasonException {
        public NotFound(String logMessage) {
            super(
                    HttpStatus.NOT_FOUND,
                    "SEASON_NOT_FOUND",
                    "해당 시즌 정보를 찾을 수 없습니다.",
                    logMessage
            );
        }
    }
}
