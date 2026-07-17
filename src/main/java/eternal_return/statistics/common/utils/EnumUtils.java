package eternal_return.statistics.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumUtils {
    public static <E extends Enum<E>> E min(E current, E candidate) {
        if (current == null) return candidate;
        if (candidate == null) return current;
        return candidate.ordinal() < current.ordinal() ? candidate : current;
    }

    public static <E extends Enum<E>> E max(E current, E candidate) {
        if (current == null) return candidate;
        if (candidate == null) return current;
        return candidate.ordinal() > current.ordinal() ? candidate : current;
    }

    public static <E extends Enum<E>> List<E> listBetween(Class<E> enumClass, E start, E end) {
        int from = Math.min(start.ordinal(), end.ordinal());
        int to = Math.max(start.ordinal(), end.ordinal()) + 1;
        return new ArrayList<>(Arrays.asList(enumClass.getEnumConstants()).subList(from, to));
    }
}
