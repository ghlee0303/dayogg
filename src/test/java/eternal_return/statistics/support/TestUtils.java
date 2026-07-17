package eternal_return.statistics.support;

import eternal_return.statistics.core.thread.ThreadExecutor;
import eternal_return.statistics.support.race_condtion.RaceJob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class TestUtils {

    public static <T> List<Exception> raceCondition(
            ThreadExecutor threadExecutor, List<T> params, ParamRunnableWithException<T> task
    ) {
        List<Exception> exceptions = new CopyOnWriteArrayList<>();
        List<Future<?>> futures = new ArrayList<>();

        for (T param : params) {
            futures.add(threadExecutor.submit(() -> {
                try {
                    task.run(param);
                } catch (Exception e) {
                    exceptions.add(e);
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
            exceptions.add(e);
        }

        return exceptions;
    }

    public static <T, K> List<RaceJob<T, K>> raceCondition(
            ThreadExecutor threadExecutor, List<RaceJob<T, K>> params, ParamFunctionWithException<T, K> task
    ) {
        long start = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();

        for (RaceJob<T, K> param : params) {
            futures.add(threadExecutor.submit(() -> {
                param.waitJob();
                try {
                    K result = task.apply(param.getParam());
                    param.complete(System.currentTimeMillis() - start, result);
                } catch (Exception e) {
                    param.exception(System.currentTimeMillis() - start, e);
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


        return params;
    }

    @FunctionalInterface
    public interface ParamRunnableWithException<T> {
        void run(T param) throws Exception;
    }

    @FunctionalInterface
    public interface ParamFunctionWithException<T, K> {
        K apply(T param) throws Exception;
    }
}