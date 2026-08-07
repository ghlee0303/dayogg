package eternal_return.dayogg.meta.meta.phase;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 패치별 페이즈 지속시간 메타.
 *
 * <p>
 * {@code phase.json}의 key는 패치 적용 일자(Unix Timestamp, 초), value는 해당 패치의
 * 페이즈 지속시간을 순서대로 나열한 배열이다. 게임 시작 시각으로 그 시점에 적용되던
 * 패치의 {@link PhaseDuration}을 선택한다.
 * </p>
 */
public class PhaseMeta {

    /** {@code startDtm}(offset 없는 LocalDateTime)을 epoch로 변환할 때 기준 타임존. */
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final NavigableMap<Long, PhaseDuration> map;

    public PhaseMeta(Map<Long, PhasePatchDto> source) {
        NavigableMap<Long, PhaseDuration> map = new TreeMap<>();
        source.forEach((patchEpoch, dto) ->
                map.put(patchEpoch, new PhaseDuration(dto.durations(), CreditRegen.from(dto.creditRegenMs()))));
        this.map = map;
    }

    /**
     * {@code startDtm} 시점에 적용되던 패치의 페이즈 지속시간을 반환한다.
     * (시작 시각 이하인 패치 중 가장 최신 것)
     */
    public PhaseDuration resolve(LocalDateTime startDtm) {
        long epoch = startDtm.atZone(ZONE).toEpochSecond();
        Map.Entry<Long, PhaseDuration> entry = map.floorEntry(epoch);
        if (entry == null) {
            throw new IllegalStateException("적용 가능한 페이즈 패치가 없습니다: " + startDtm);
        }
        return entry.getValue();
    }
}
