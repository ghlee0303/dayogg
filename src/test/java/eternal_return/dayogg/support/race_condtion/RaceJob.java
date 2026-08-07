package eternal_return.dayogg.support.race_condtion;

import eternal_return.dayogg.common.utils.MathUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class RaceJob<T, K> {
    private final T param;
    private long waitMillis;

    private long completeMillis;
    private K result;
    private Exception exception;

    public RaceJob(T param, long waitMillis) {
        this.param = param;
        this.waitMillis = waitMillis;
    }

    public void waitJob() {
        if (waitMillis == 0) return;

        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void complete(long completeMillis, K result) {
        this.completeMillis = completeMillis;
        this.result = result;
    }

    public void exception(long completeMillis, Exception exception) {
        this.completeMillis = completeMillis;
        this.exception = exception;
    }

    public static <T, K> List<RaceJob<T, K>> createList(
            List<T> params, long waitMillisStart, long waitMillisEnd
    ) {
        List<RaceJob<T, K>> result = new ArrayList<>();

        for (T param : params) {
            long waitMillis = MathUtils.random(waitMillisStart, waitMillisEnd);
            result.add(new RaceJob<>(param, waitMillis));
        }

        return result;
    }
}
