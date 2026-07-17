package eternal_return.statistics.core.exception.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionResponseEnum {
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서비스에 오류가 발생하였습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "현재 접속자가 많아 서비스가 지연되고 있습니다. 잠시 후 다시 시도해 주세요."),
    ROUTE_AUTH_DUPLICATE(HttpStatus.CONFLICT, "이미 루트 인증이 진행중입니다. 20분 뒤 다시 시도해주세요."),

    ;
    private final HttpStatus status;
    private final String responseMessage;
}
