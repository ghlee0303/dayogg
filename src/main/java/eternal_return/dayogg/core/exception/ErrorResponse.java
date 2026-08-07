package eternal_return.dayogg.core.exception;

public record ErrorResponse(
        Integer status,
        String message,
        String type
) {
    public static ErrorResponse fromBusiness(BusinessException e) {
        return new ErrorResponse(
                e.getHttpStatus().value(),
                e.getResponseMessage(),
                e.getErrorType()
        );
    }

    public static ErrorResponse unexpected(String message) {
        return new ErrorResponse(
                500, message, "unexpected"
        );
    }
}