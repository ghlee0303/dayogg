package eternal_return.dayogg.common.log;

import java.util.LinkedHashMap;
import java.util.Map;

public class LogContext {
    private static final ThreadLocal<Map<String, Object>> ctx =
            ThreadLocal.withInitial(LinkedHashMap::new);

    /** {@code @ServiceLogging} 중첩 깊이. 가장 바깥 프레임이 누구인지 판단하는 데만 쓴다. */
    private static final ThreadLocal<int[]> depth =
            ThreadLocal.withInitial(() -> new int[1]);

    /**
     * {@code @ServiceLogging} 프레임에 진입했음을 알린다.
     *
     * @return 이 프레임이 이 스레드의 최외곽이면 true — flush 와 로그 한 줄은 여기서만 나간다
     */
    public static boolean enter() {
        return ++depth.get()[0] == 1;
    }

    /**
     * 프레임에서 빠져나왔음을 알린다. 반드시 {@code finally} 에서 부른다.
     *
     * <p>최외곽이 나가면 컨텍스트까지 정리한다 — 로깅이 중간에 실패해도 값이 다음으로 새지 않는다.
     */
    public static void exit() {
        if (--depth.get()[0] <= 0) {
            depth.remove();
            ctx.remove();
        }
    }

    public static void put(String key, Object value) {
        ctx.get().put(key, value);
    }

    /** 먼저 쓴 값이 이긴다. 실패 발원지({@code failedAt})는 가장 안쪽 프레임이 먼저 잡는다. */
    public static void putIfAbsent(String key, Object value) {
        ctx.get().putIfAbsent(key, value);
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

    /**
     * 스냅샷 맵을 반환한 뒤 컨텍스트를 비운다.
     *
     * <p>문자열로 평탄화하지 않으므로 값의 타입이 보존된다 — 숫자는 JSON 숫자 필드로 나간다.
     */
    public static Map<String, Object> drainMap() {
        Map<String, Object> map = new LinkedHashMap<>(ctx.get());
        ctx.remove();

        return map;
    }
}
