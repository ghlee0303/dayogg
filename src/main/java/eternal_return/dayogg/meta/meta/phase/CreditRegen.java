package eternal_return.dayogg.meta.meta.phase;

import java.util.HashMap;
import java.util.Map;

/**
 * 페이즈 인덱스별 크레딧 1개당 재생 주기(ms).
 *
 * <p>{@code defaultMs}는 별도 지정이 없는 페이즈에 적용되는 기본값,
 * {@code overrides}는 특정 페이즈 인덱스에만 적용되는 값이다.</p>
 */
public record CreditRegen(int defaultMs, Map<Integer, Integer> overrides) {

    /** {@code phase.json}의 평탄한 맵({@code "default"}, {@code "0"}, {@code "1"}...)을 분리해 생성한다. */
    public static CreditRegen from(Map<String, Integer> source) {
        Integer defaultMs = source.get("default");
        if (defaultMs == null) {
            throw new IllegalArgumentException("credit_regen_ms에 default 값이 없습니다: " + source);
        }

        Map<Integer, Integer> overrides = new HashMap<>();
        source.forEach((key, value) -> {
            if ("default".equals(key)) return;
            overrides.put(Integer.parseInt(key), value);
        });

        return new CreditRegen(defaultMs, overrides);
    }

    /** {@code phaseIndex} 페이즈의 크레딧 1개당 재생 주기(ms). 지정이 없으면 기본값. */
    public int msAt(int phaseIndex) {
        return overrides.getOrDefault(phaseIndex, defaultMs);
    }
}
