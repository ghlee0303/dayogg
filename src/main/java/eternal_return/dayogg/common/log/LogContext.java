package eternal_return.dayogg.common.log;

import eternal_return.dayogg.common.utils.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class LogContext {
    private static final ThreadLocal<Map<String, Object>> ctx =
            ThreadLocal.withInitial(LinkedHashMap::new);

    public static void remove(String key) {
        ctx.get().remove(key);
    }

    public static void removeList(String[] keyList) {
        for (String key : keyList) {
            ctx.get().remove(key);
        }
    }

    public static void put(String key, Object value) {
        ctx.get().put(key, value);
    }

    public static void addLong(String key, Long add) {
        Object target = ctx.get().get(key);

        if (target == null) {
            put(key, add);
            return;
        }

        if (!(target instanceof Long))
            return;

        put(key, (Long) target + add);
    }

    public static String toStringAndClear(String[] keyList) {
        Map<String, Object> map = new LinkedHashMap<>(ctx.get());
        removeList(keyList);

        StringBuilder sb = new StringBuilder();
        for (String key : keyList) {
            sb.append(key)
                    .append(": ")
                    .append(map.get(key))
                    .append(" | ");
        }

        return sb.toString();
    }

    /**
     * 문자열로 평탄화하지 않고 스냅샷 맵을 반환한 뒤 컨텍스트를 비운다.
     *
     * <p>{@link #toStringAndClear()} 와 달리 값의 타입이 보존되므로
     * 숫자는 JSON 숫자 필드로 나갈 수 있다.
     */
    public static Map<String, Object> drainMap() {
        Map<String, Object> map = new LinkedHashMap<>(ctx.get());
        ctx.remove();

        return map;
    }

    public static String toStringAndClear() {
        Map<String, Object> map = new LinkedHashMap<>(ctx.get());
        ctx.remove();

        return MapUtils.toString(map);
    }
}
