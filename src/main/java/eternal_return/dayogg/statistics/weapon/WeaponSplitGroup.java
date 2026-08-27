package eternal_return.dayogg.statistics.weapon;

import java.util.HashMap;
import java.util.Map;

/**
 * 착용 무기 아이템 코드 → 대표 아이템 코드 정규화.
 *
 * <p>외부 API의 {@code bestWeapon}({@code weaponNum})만으로는 구별되지 않지만 통계에서는
 * 나눠 봐야 하는 무기군을 대표 코드로 묶는다. 등급 등 세부 아이템 차이는 대표 코드로 흡수한다.
 * 분리 대상 무기군은 {@link WeaponGroupEnum}으로 정의한다.</p>
 */
public final class WeaponSplitGroup {

    /** 아이템 코드 → 무기군 대표 코드 */
    private static final Map<Integer, Integer> REPRESENTATIVE_BY_CODE = new HashMap<>();

    static {
        for (WeaponGroupEnum weapon : WeaponGroupEnum.values()) {
            for (Integer itemCode : weapon.itemCodeList()) {
                REPRESENTATIVE_BY_CODE.put(itemCode, weapon.representativeCode());
            }
        }
    }

    private WeaponSplitGroup() {
    }

    /** 등록된 무기군에 속하면 대표 코드, 아니면 {@code null}. 대표 코드를 넣으면 그대로 반환한다. */
    public static Integer representativeOf(Integer equipWeaponCode) {
        return equipWeaponCode == null ? null : REPRESENTATIVE_BY_CODE.get(equipWeaponCode);
    }
}
