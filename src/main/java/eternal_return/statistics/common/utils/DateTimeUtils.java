package eternal_return.statistics.common.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateTimeUtils {

    /** "yyyy-MM-dd" 또는 "yyyy-MM-dd HH:mm:ss"를 모두 받는 포맷. 시간 누락 시 00:00:00. */
    public static final DateTimeFormatter YMD_OR_YMD_HMS = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm:ss")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    public static ZonedDateTime toZoned(String text, DateTimeFormatter otherFormatter) {
        return ZonedDateTime.parse(text, otherFormatter);
    }

    public static ZonedDateTime toZoned(String text) {
        return ZonedDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    }

    public static LocalDateTime toLocal(String text, DateTimeFormatter otherFormatter) {
        return LocalDateTime.parse(text, otherFormatter);
    }

    public static LocalDateTime toLocal(String text) {
        return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static LocalDateTime zonedTextToLocal(String text, DateTimeFormatter otherFormatter) {
        return toZoned(text, otherFormatter)
                .toLocalDateTime();
    }

    public static String fromLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public static boolean between(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        return target.isAfter(start) && target.isBefore(end);
    }

    public static LocalDateTime min(LocalDateTime current, LocalDateTime candidate) {
        if (current == null) return candidate;
        if (candidate == null) return current;
        return candidate.isBefore(current) ? candidate : current;
    }

    public static LocalDateTime max(LocalDateTime current, LocalDateTime candidate) {
        if (current == null) return candidate;
        if (candidate == null) return current;
        return candidate.isAfter(current) ? candidate : current;
    }
}
