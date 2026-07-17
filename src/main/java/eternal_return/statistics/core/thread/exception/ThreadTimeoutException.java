package eternal_return.statistics.core.thread.exception;

import eternal_return.statistics.core.exception.enums.LogMessageEnum;
import lombok.Getter;

@Getter
public class ThreadTimeoutException extends RuntimeException {
    final int waitingCount;

    public ThreadTimeoutException(int waitingCount) {
        super(LogMessageEnum.TIMEOUT_THREAD.format(waitingCount));
        this.waitingCount = waitingCount;
    }

    public String getStringWaitingCount() {
        return String.valueOf(waitingCount);
    }
}
