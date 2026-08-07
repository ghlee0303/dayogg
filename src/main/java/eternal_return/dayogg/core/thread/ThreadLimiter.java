package eternal_return.dayogg.core.thread;

import eternal_return.dayogg.core.thread.exception.ThreadInterruptedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 가상 쓰레드 동시 실행 수를 제한하는 Semaphore 래퍼.
 *
 * <p>가상 쓰레드는 수십만 개까지 생성 가능하지만, 외부 API 호출·DB 부하 등을
 * 고려해 동시 실행 상한을 설정한다. 허용 수({@code maxPermits})는
 * {@code application.yml}의 {@code api.thread-max-permits}로 조정한다.
 */
@Component
public class ThreadLimiter {
    private final int maxPermits;
    private final Semaphore semaphore;
    private final int timeoutSeconds;

    public ThreadLimiter(
            @Value("${thread.max-permits}") int maxPermits,
            @Value("${thread.timeout-seconds}") int timeoutSeconds
    ) {
        this.maxPermits = maxPermits;
        this.semaphore = new Semaphore(maxPermits);
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 지정한 시간(초) 내에 퍼밋 획득을 시도한다.
     *
     * @return 퍼밋 획득 성공 시 true, 타임아웃 시 false
     * @throws ThreadInterruptedException 대기 중 쓰레드가 인터럽트된 경우
     */
    public boolean tryAcquire() {
        try {
            return semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ThreadInterruptedException();
        }
    }

    /**
     * 작업 완료 후 퍼밋을 반환한다. 반드시 {@code finally} 블록에서 호출해야 한다.
     */
    public void release() {
        semaphore.release();
    }

    /** 퍼밋 획득을 대기 중인 쓰레드 수 */
    public int getWaitingCount() {
        return semaphore.getQueueLength();
    }

    /** 현재 실행 중인 작업 수 */
    public int getActiveCount() {
        return maxPermits - semaphore.availablePermits();
    }
}