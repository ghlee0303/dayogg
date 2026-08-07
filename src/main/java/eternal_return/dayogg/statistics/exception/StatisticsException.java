package eternal_return.dayogg.statistics.exception;

import eternal_return.dayogg.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class StatisticsException extends BusinessException {
    public StatisticsException(
            HttpStatus httpStatus,
            String errorType,
            String responseMessage,
            String logMessage
    ) {
        super(httpStatus, errorType, responseMessage, logMessage);
    }

    public static class InvalidRange extends StatisticsException {
        public InvalidRange(String logMessage) {
            super(
                    HttpStatus.BAD_REQUEST,
                    "STATISTICS_INVALID_RANGE",
                    "잘못된 조회 구간 입니다.",
                    logMessage
            );
        }
    }
}
