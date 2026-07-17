package eternal_return.statistics.support.race_condtion;

import eternal_return.statistics.core.thread.ThreadExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class RaceCondition {
    @Autowired
    private ThreadExecutor threadExecutor;

    public <T, K> void start(
            List<RaceJob<T, K>> jobList, ParamFunctionWithException<T, K> task
    ) {
        long start = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();

        for (RaceJob<T, K> job : jobList) {
            futures.add(threadExecutor.submit(() -> {
                job.waitJob();
                try {
                    K result = task.apply(job.getParam());
                    job.complete(System.currentTimeMillis() - start, result);
                } catch (Exception e) {
                    job.exception(System.currentTimeMillis() - start, e);
                }
            }));
        }

        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // 개별 결과는 각 param에 이미 기록됨
        }
    }


    @FunctionalInterface
    public interface ParamFunctionWithException<T, K> {
        K apply(T param) throws Exception;
    }
}
