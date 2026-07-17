package eternal_return.statistics.common.utils;

import java.util.*;

public class MapUtils {
    public static <T, K> Map<T, K> copyMap(Map<T, K> originalMap) {
        Map<T, K> copiedMap = new HashMap<>();

        for (T key : originalMap.keySet()) {
            copiedMap.put(key, originalMap.get(key));
        }

        return copiedMap;
    }

    public static <T, K> Optional<K> findMax(
            Map<T, K> targetMap, Comparator<K> comparator
    ) {
        return targetMap.values().stream()
                .max(comparator);
    }

    public static <T, K> Optional<K> findMin(
            Map<T, K> targetMap, Comparator<K> comparator
    ) {
        return targetMap.values().stream()
                .min(comparator);
    }

    public static String toString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            sb.append(key)
                    .append(": ")
                    .append(map.get(key))
                    .append(" | ");
        }

        return sb.toString();
    }
}
