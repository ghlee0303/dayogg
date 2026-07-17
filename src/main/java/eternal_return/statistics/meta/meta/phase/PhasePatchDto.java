package eternal_return.statistics.meta.meta.phase;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * {@code phase.json}의 패치별 엔트리 역직렬화용 DTO.
 *
 * <p>{@code credit_regen_ms}는 {@code "default"}와 페이즈 인덱스 문자열을 키로 갖는 평탄한 맵이다.</p>
 */
public record PhasePatchDto(
        List<Integer> durations,
        @JsonProperty("credit_regen_ms") Map<String, Integer> creditRegenMs
) {
}
