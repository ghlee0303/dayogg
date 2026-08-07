package eternal_return.dayogg.core.idempotent;

import java.util.ArrayList;
import java.util.List;

/**
 * @param jobId 잡 실행 1회를 가리키는 추적 축. 합류하는 요청도 여기서 읽어가야 하므로 Redis 에 함께 실린다.
 *              {@code key} 는 잡 종료 후 재사용되지만 {@code jobId} 는 실행마다 새로 발급된다.
 */
public record Idempotent(
        String key,
        String jobId,
        List<String> sseKeyList
) {
    public static Idempotent create(String key, String jobId, String firstSseKey) {
        return new Idempotent(
                key,
                jobId,
                new ArrayList<>(List.of(firstSseKey))
        );
    }

    public void addSseKey(String sseKey) {
        if (!sseKeyList.contains(sseKey)) {
            sseKeyList.add(sseKey);
        }
    }
}