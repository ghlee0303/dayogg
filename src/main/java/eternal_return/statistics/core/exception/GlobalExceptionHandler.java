package eternal_return.statistics.core.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        log.warn("[EXCEPTION] {}", e.getLogMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.fromBusiness(e));
    }
}
