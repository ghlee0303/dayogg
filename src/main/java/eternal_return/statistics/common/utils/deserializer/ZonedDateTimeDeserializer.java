package eternal_return.statistics.common.utils.deserializer;

import eternal_return.statistics.common.utils.DateTimeUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeDeserializer extends ValueDeserializer<ZonedDateTime> {
    private final DateTimeFormatter formatter;

    public ZonedDateTimeDeserializer() {
        this.formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    }

    public ZonedDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public ZonedDateTime deserialize(
            JsonParser p,
            DeserializationContext ctxt
    ) {
        String value = p.getValueAsString();

        return DateTimeUtils.toZoned(value, formatter);
    }

    // 특정 포맷용 서브클래스
    public static class YmdHms extends ZonedDateTimeDeserializer {
        public YmdHms() {
            super(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        }
    }
}
