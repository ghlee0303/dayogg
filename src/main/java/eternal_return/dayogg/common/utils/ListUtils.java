package eternal_return.dayogg.common.utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ListUtils {
    public static <T> Optional<T> findByDateTimeRange(
            List<T> list,
            LocalDateTime target,
            Function<T, LocalDateTime> startExtractor,
            Function<T, LocalDateTime> endExtractor
    ) {
        return list.stream()
                .filter(item -> DateTimeUtils.between(target, startExtractor.apply(item), endExtractor.apply(item)))
                .findFirst();
    }

    public static <T, K> Optional<T> findById(
            List<T> list,
            K target,
            Function<T, K> idExtractor
    ) {
        return list.stream()
                .filter(item -> idExtractor.apply(item).equals(target))
                .findFirst();
    }

    public static <T> Optional<T> findByDateTimeAfter(
            List<T> list,
            LocalDateTime target,
            Function<T, LocalDateTime> extractor
    ) {
        return list.stream()
                .filter(item -> target.isAfter(extractor.apply(item)))
                .findFirst();
    }

    public static Integer sum(
            List<Integer> list,
            int maxIndex
    ) {
        int total = 0;
        int end = Math.min(maxIndex, list.size() - 1);

        for (int i = 0; i <= end; i++) {
            total += list.get(i);
        }

        return total;
    }

    public static <T> int count(
            List<T> list,
            T target
    ) {
        int count = 0;
        for (T item : list) {
            if (Objects.equals(item, target)) count++;
        }

        return count;
    }
}
