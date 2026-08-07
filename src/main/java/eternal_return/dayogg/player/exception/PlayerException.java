package eternal_return.dayogg.player.exception;

import eternal_return.dayogg.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class PlayerException extends BusinessException {
    public PlayerException(
            HttpStatus httpStatus,
            String errorType,
            String responseMessage,
            String logMessage
    ) {
        super(httpStatus, errorType, responseMessage, logMessage);
    }

    public static class NotFound extends PlayerException {
        public NotFound(String logMessage) {
            super(
                    HttpStatus.NOT_FOUND,
                    "PLAYER_NOT_FOUND",
                    "해당 유저 정보를 찾을 수 없습니다.",
                    logMessage
            );
        }
    }
 }
