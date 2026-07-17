package eternal_return.statistics.core.idempotent.exception;

import lombok.Getter;

@Getter
public class IdempotentException extends RuntimeException {
    String idempotentKey;

    public IdempotentException(String idempotentKey, String message) {
        super(message);
        this.idempotentKey = idempotentKey;
    }

    public String getMessage() {
        return super.getMessage();
    }
}
