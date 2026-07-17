package eternal_return.statistics.core.sse.enums;

/**
 * SSE 이벤트 타입 정의.
 *
 * <p>클라이언트는 {@code EventSource}의 {@code addEventListener(type, handler)}로
 * 각 타입을 구분하여 처리할 수 있다.
 */
public enum SseStatus {

    /** 기본 메시지 이벤트. 특별한 타입이 지정되지 않은 경우 사용된다. */
    MESSAGE,

    /** 구독 성공 이벤트. SSE 연결이 수립되는 즉시 전송되어 클라이언트가 연결을 확인할 수 있다. */
    OPEN,

    /** 오류 이벤트. 작업 처리 중 예외가 발생했을 때 오류 메시지와 함께 전송된다. */
    ERROR,
    ;

    public String getName() {
        return this.name().toLowerCase();
    }
}