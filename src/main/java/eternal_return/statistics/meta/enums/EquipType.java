package eternal_return.statistics.meta.enums;

/**
 * 장비 타입(무기/방어구)을 하나로 다루기 위한 공통 계약.
 *
 * <p>{@link WeaponEnum}(무기 종류)과 {@link ArmorEnum}(방어구 부위)이 구현한다.
 * 두 enum은 의미 축이 다르지만 {@link #category()}로 대분류를 구분할 수 있어,
 * {@code EquipInfo}가 단일 필드로 무기·방어구를 함께 담을 수 있다.</p>
 */
public sealed interface EquipType permits WeaponEnum, ArmorEnum {
    EquipCategory category();
}
