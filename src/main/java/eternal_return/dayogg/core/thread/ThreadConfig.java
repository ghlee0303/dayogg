package eternal_return.dayogg.core.thread;

import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 가상 쓰레드 기반 {@link ExecutorService} 설정.
 *
 * <p>{@code spring.threads.virtual.enabled=true} 환경에서만 빈이 등록된다.
 * 각 태스크 제출 시 새 가상 쓰레드를 생성하므로 OS 쓰레드 풀 크기에 무관하게
 * 높은 동시성을 처리할 수 있다.
 *
 * <p>동시 실행 상한은 {@link ThreadLimiter}의 Semaphore가 담당한다.
 * {@code destroyMethod = "shutdown"}으로 애플리케이션 종료 시 Executor를 정상 종료한다.
 */
@Configuration
public class ThreadConfig {

    /**
     * 가상 쓰레드 Executor 빈.
     * 쓰레드 이름 형식: {@code vt-worker-0}, {@code vt-worker-1}, ...
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnThreading(Threading.VIRTUAL)
    public ExecutorService virtualThreadExecutor() {
        ThreadFactory factory = Thread.ofVirtual().name("vt-worker-", 0).factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }
}