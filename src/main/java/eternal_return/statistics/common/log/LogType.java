package eternal_return.statistics.common.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogType {
    MILLIS("ms"),
    STRING(""),
    ;

    private final String format;
}
