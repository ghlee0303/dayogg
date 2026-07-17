package eternal_return.statistics.meta.meta.phase;

import java.util.List;

/**
 * 한 패치에 적용되는 페이즈 지속시간(초) 목록과 페이즈별 크레딧 재생 주기.
 *
 * <p>앞에서부터 순서대로 페이즈가 진행되며, DAY/NIGHT 구분 없이 인덱스로만 다룬다.</p>
 */
public record PhaseDuration(List<Integer> durations, CreditRegen creditRegen) {

    /**
     * 앞에서부터 {@code phaseCount}개 페이즈의 누적 지속시간(초).
     *
     * <p>예: 1일차 밤까지 = {@code secondsUntil(2)}, 5일차 밤까지 = {@code secondsUntil(10)}</p>
     */
    public int secondsUntil(int phaseCount) {
        int limit = Math.min(phaseCount, durations.size());
        int sum = 0;
        for (int i = 0; i < limit; i++) {
            sum += durations.get(i);
        }
        return sum;
    }

    /** {@code phaseIndex} 페이즈의 크레딧 1개당 재생 주기(ms). */
    public int creditRegenMsAt(int phaseIndex) {
        return creditRegen.msAt(phaseIndex);
    }
}
