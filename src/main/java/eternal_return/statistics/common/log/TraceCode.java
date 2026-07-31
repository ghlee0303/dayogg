package eternal_return.statistics.common.log;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 로그 추적 축에 쓰는 6자리 영숫자 코드 생성기.
 *
 * <p>요청 축({@code code})과 SSE 잡 축({@code jobId})이 같은 형식을 쓴다.
 * UUID 를 쓰지 않는 이유는 두 가지다 — 36자는 CloudWatch 수집량(=비용)에 불리하고,
 * 두 축의 형식이 다르면 로그를 눈으로 훑을 때 걸린다.
 *
 * <p>경우의 수는 36<sup>6</sup> ≈ 21억이다. 동시에 살아 있는 요청·잡 사이의 충돌은 무시할 수준이며,
 * 영구 식별자가 아니라 조회용 축이므로 전역 유일성까지는 필요하지 않다.
 */
public final class TraceCode {

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int LENGTH = 6;

    private TraceCode() {
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}
