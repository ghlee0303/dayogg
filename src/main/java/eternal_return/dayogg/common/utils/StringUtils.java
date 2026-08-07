package eternal_return.dayogg.common.utils;

public class StringUtils {

    public static String format(String format, Object... args) {
        String result = format;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", String.valueOf(arg));
        }
        return result;
    }

    public static boolean contains(String a, String b) {
        if (a == null || b == null) return false;
        return a.contains(b);
    }

}
