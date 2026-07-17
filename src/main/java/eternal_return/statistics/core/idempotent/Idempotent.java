package eternal_return.statistics.core.idempotent;

import java.util.ArrayList;
import java.util.List;

public record Idempotent(
        String key,
        List<String> sseKeyList
) {
    public static Idempotent create(String key, String firstSseKey) {
        return new Idempotent(
                key,
                new ArrayList<>(List.of(firstSseKey))
        );
    }

    public void addSseKey(String sseKey) {
        if (!sseKeyList.contains(sseKey)) {
            sseKeyList.add(sseKey);
        }
    }
}