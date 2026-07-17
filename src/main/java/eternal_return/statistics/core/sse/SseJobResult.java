package eternal_return.statistics.core.sse;

import eternal_return.statistics.core.idempotent.enums.IdempotentStatus;
import org.springframework.http.HttpStatus;

public record SseJobResult(
        String message,
        HttpStatus httpStatus
) {
    public SseJobResult(IdempotentStatus idempotentStatus, HttpStatus httpStatus) {
        this(idempotentStatus.name(), httpStatus);
    }

    public SseJobResult(IdempotentStatus idempotentStatus) {
        this(idempotentStatus.name(), HttpStatus.ACCEPTED);
    }

    public SseJobResult(String message) {
        this(message, HttpStatus.OK);
    }
}
