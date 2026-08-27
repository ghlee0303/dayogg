package eternal_return.dayogg.statistics.weapon;

import java.util.List;

/**
 * {@code bestWeapon}(무기 종류)이 같아 통계상 한 덩어리가 되지만 실제로는 나눠 봐야 하는 무기군.
 *
 * <p>각 상수는 한 무기군에 속하는 아이템 코드(등급별 등)를 가지며, 첫 코드를 대표 코드로 쓴다.
 * 캐릭터별 구분은 주석으로 한다. 새 대상이 생기면 상수만 추가하면 {@link WeaponSplitGroup}이
 * 자동으로 인식한다.</p>
 */
public enum WeaponGroupEnum {
    // TODO: 음수 placeholder 를 실제 ItemWeapon 코드로 교체 (v2/data/ItemWeapon). WeaponSplitGroupTest 기대값도 함께 수정.

    // 에키온
    DEATH_ADDER(131301, 131401, 131501, 131502, 131503),
    BLACK_MAMBA(131302, 131402, 131504, 131505, 131506),
    SIDEWINDER(131303, 131403, 131507, 131508, 131509),
    ;

    private final List<Integer> itemCodeList;

    WeaponGroupEnum(Integer... itemCodes) {
        this.itemCodeList = List.of(itemCodes);
    }

    public Integer representativeCode() {
        return itemCodeList.getFirst();
    }

    public List<Integer> itemCodeList() {
        return itemCodeList;
    }
}
