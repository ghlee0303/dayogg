package eternal_return.statistics.meta.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 아이템 등급을 나타내는 Enum.
 * <p>
 * 상수명(name)은 도메인 의미이자 응답으로 나가는 값이고,
 * {@code externalValue}는 외부 API의 {@code itemGrade} 값이다.
 * <ul>
 *     <li>역직렬화(요청 수신): {@link #from(String)} 이 외부 값("Common")을 상수로 매핑한다.</li>
 *     <li>직렬화(응답 전송): Jackson 기본 동작으로 상수명(COMMON)을 그대로 내보낸다.</li>
 * </ul>
 */
public enum ItemGradeEnum {
    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGEND("Legend"),
    MYTHIC("Mythic"),
    ;

    private final String externalValue;

    ItemGradeEnum(String externalValue) {
        this.externalValue = externalValue;
    }

    @JsonCreator
    public static ItemGradeEnum from(String value) {
        for (ItemGradeEnum grade : values()) {
            if (grade.externalValue.equalsIgnoreCase(value)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Unknown item grade: " + value);
    }
}
