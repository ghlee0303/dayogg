package eternal_return.statistics.tier.exception;

import eternal_return.statistics.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TierException extends BusinessException {
    public TierException(
            HttpStatus httpStatus,
            String errorType,
            String responseMessage,
            String logMessage
    ) {
        super(httpStatus, errorType, responseMessage, logMessage);
    }

    public static class RangeOut extends TierException {
        public RangeOut(String logMessage) {
            super(
                    HttpStatus.BAD_REQUEST,
                    "TIER_RANGE_OUT",
                    "잘못된 티어 범위 입니다.",
                    logMessage
            );
        }
    }
}
