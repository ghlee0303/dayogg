package eternal_return.statistics.core.thread;

import eternal_return.statistics.core.thread.exception.ThreadInterruptedException;
import eternal_return.statistics.core.thread.exception.ThreadTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 가상 쓰레드 작업 제출기.
 *
 * <p>{@link ThreadLimiter}로 동시 실행 수를 제한하면서 작업을
 * {@link ExecutorService}에 제출한다.
 *
 * <pre>
 * tryAcquire(timeout)
 *   성공               → virtualThreadExecutor.submit(task)
 *                           └─ finally: threadLimiter.release()
 *   실패(타임아웃)      → {@link ThreadTimeoutException}
 *   실패(인터럽트)      → {@link ThreadInterruptedException}
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class ThreadExecutor {

    private final ExecutorService virtualThreadExecutor;
    private final ThreadLimiter threadLimiter;

    /**
     * Semaphore 퍼밋을 획득한 뒤 작업을 가상 쓰레드에 제출한다.
     *
     * <p>작업이 정상 완료되거나 예외로 종료되더라도 {@code finally}에서
     * 퍼밋을 반환하여 대기 중인 다른 작업이 실행될 수 있도록 한다.
     *
     * @param task 가상 쓰레드에서 실행할 작업
     * @return 제출된 작업의 {@link Future}
     * @throws ThreadTimeoutException     타임아웃으로 퍼밋 획득에 실패한 경우
     * @throws ThreadInterruptedException 대기 중 쓰레드가 인터럽트된 경우
     */
    public Future<?> submit(Runnable task) {
        boolean acquired = threadLimiter.tryAcquire();

        if (!acquired) {
            throw new ThreadTimeoutException(threadLimiter.getWaitingCount());
        }

        return virtualThreadExecutor.submit(() -> {
            try {
                task.run();
            } finally {
                // 작업 종료 시 항상 퍼밋 반환 — 예외 발생 여부와 무관하게 실행
                threadLimiter.release();
            }
        });
    }
}