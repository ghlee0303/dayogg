package eternal_return.dayogg.common.utils.deserializer;

import eternal_return.dayogg.common.utils.DateTimeUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {
    private final DateTimeFormatter formatter;

    public LocalDateTimeDeserializer() {
        this.formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    public LocalDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public LocalDateTime deserialize(
            JsonParser p,
            DeserializationContext ctxt
    ) {
        String value = p.getValueAsString();

        try {
            return DateTimeUtils.toZoned(value).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return DateTimeUtils.toLocal(value, formatter);
        }
    }

    // Local 포맷용 서브클래스
    // "yyyy-MM-dd" 또는 "yyyy-MM-dd HH:mm:ss" 둘 다 허용. 시간 누락 시 00:00:00으로 채움.
    public static class YmdHms extends LocalDateTimeDeserializer {
        public YmdHms() {
            super(DateTimeUtils.YMD_OR_YMD_HMS);
        }
    }

    public static class Epoch extends LocalDateTimeDeserializer {
        public Epoch() {
            super(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
    }

    // Zoned 포맷용 서브클래스
    public static class ZonedYmdHms extends LocalDateTimeDeserializer {
        public ZonedYmdHms() {
            super(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        }
    }
}
