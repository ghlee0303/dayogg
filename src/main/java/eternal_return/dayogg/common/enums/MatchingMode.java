package eternal_return.dayogg.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MatchingMode {
    NORMAL(2),
    RANK(3),
    COBALT(6),
    UNION(8),
    LONE_WOLF(9),
    UN(11),
    NEW_MODE(99),
    ;

    private final int code;

    MatchingMode(int code) {
        this.code = code;
    }

    @JsonCreator
    public static MatchingMode from(int code) {
        for (MatchingMode type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MatchingMode: " + code);
    }

    @JsonValue
    public int toValue() {
        return code;
    }

    public boolean isRankOrNormal() {
        return this == MatchingMode.NORMAL || this == MatchingMode.RANK;
    }

    public boolean isRank() {
        return this == MatchingMode.RANK;
    }
}
