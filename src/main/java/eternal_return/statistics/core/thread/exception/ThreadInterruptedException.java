package eternal_return.statistics.core.thread.exception;

/**
 * 퍼밋 대기 중 쓰레드가 인터럽트되었을 때 발생하는 예외.
 *
 * <p>{@link eternal_return.statistics.core.thread.ThreadTimeOutException}(타임아웃)과
 * 구분하여 호출부가 인터럽트 원인을 별도로 처리할 수 있도록 한다.
 */
public class ThreadInterruptedException extends RuntimeException {

    public ThreadInterruptedException() {
        super("thread interrupted while waiting for permit");
    }
}