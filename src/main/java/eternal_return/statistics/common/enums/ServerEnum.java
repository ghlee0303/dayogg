package eternal_return.statistics.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum ServerEnum {
    ASIA(10, "Asia"),
    NA(12, "NA"),
    EU(13, "Europe"),
    SA(14, "South America"),
    ASIA2(17, "Asia2"),
    ASIA3(18, "Asia3"),
    ;

    private final int code;
    private final String name;

    ServerEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ServerEnum fromName(String name) {
        for (ServerEnum type : values()) {
            if (Objects.equals(type.name, name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ServerType Name: " + name);
    }

    @JsonCreator
    public static ServerEnum fromCode(int code) {
        for (ServerEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ServerType Code: " + code);
    }
}
