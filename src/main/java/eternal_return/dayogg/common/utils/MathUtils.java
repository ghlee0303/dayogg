package eternal_return.dayogg.common.utils;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {
    public static int twoDivide(int value1, int value2) {
        return (value1 + value2) / 2;
    }

    public static float twoDivide(float value1, int value2) {
        return (value1 + value2) / 2;
    }

    public static float twoDivideOrDefault(float value1, int value2) {
        return (value1 + value2) / 2;
    }

    public static float onlineAverage(float average, int newValue, int total) {
        return average + (newValue - average) / total;
    }

    public static float onlineAverage(float average, float newValue, int total) {
        return average + (newValue - average) / total;
    }

    public static float weightedAverage(float prevAvg, float newAvg, int prevN, int newN) {
        int total = prevN + newN;
        if (total == 0) return 0;
        return (prevAvg * prevN + newAvg * newN) / total;
    }

    public static long random(long start, long end) {
        return ThreadLocalRandom.current().nextLong(start, end);
    }

    // 0은 "데이터 없음"을 의미하므로 최솟값 계산에서 제외한다.
    public static int min(Integer a, Integer b) {
        if (a == null || a == 0) return b;
        if (b == null || b == 0) return a;
        return Math.min(a, b);
    }

    // 0은 "데이터 없음"을 의미하므로 최댓값 계산에서 제외한다.
    public static int max(Integer a, Integer b) {
        if (a == null || a == 0) return b;
        if (b == null || b == 0) return a;
        return Math.max(a, b);
    }
}
