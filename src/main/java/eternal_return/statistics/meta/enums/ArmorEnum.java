package eternal_return.statistics.meta.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 이터널리턴의 방어구 부위를 나타내는 Enum.
 * <p>
 * 상수명은 도메인 의미를, {@link JsonProperty}는 외부 API의 {@code armorType} 값을 가진다.
 */
public enum ArmorEnum implements EquipType {
    @JsonProperty("Head") HEAD,
    @JsonProperty("Chest") CHEST,
    @JsonProperty("Arm") ARM,
    @JsonProperty("Leg") LEG,
    ;

    @Override
    public EquipCategory category() {
        return EquipCategory.ARMOR;
    }
}
