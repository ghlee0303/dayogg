package eternal_return.dayogg.statistics.dto.response;

import eternal_return.dayogg.statistics.weapon.WeaponSplitGroup;
import eternal_return.dayogg.statistics.extend.AverageStatistics;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class CharacterStatisticsResponse extends AverageStatisticsResponse {
    private Integer characterNum;
    private List<WeaponResponse> weaponList;

    @Getter
    @Setter
    public static class WeaponResponse extends AverageStatisticsResponse {
        private Integer weaponNum;
        private Integer equipWeaponCode; // weaponNum이 같아도 착용 무기로 분리해야 하는 경우의 대표 아이템 코드 (예: 에키온)

        public WeaponResponse(Integer weaponNum, Integer equipWeaponCode) {
            this.weaponNum = weaponNum;
            this.equipWeaponCode = equipWeaponCode;
        }
    }

    public CharacterStatisticsResponse(Integer characterNum) {
        this.characterNum = characterNum;
        this.weaponList = new ArrayList<>();
    }

    public void merge(CharacterStatisticsResponse merged) {
        super.merge(merged);
        for (WeaponResponse mergedWeapon : merged.weaponList) {
            findOrCreateWeapon(mergedWeapon.getWeaponNum(), mergedWeapon.getEquipWeaponCode())
                    .merge(mergedWeapon);
        }
    }

    public void merge(int weaponNum, Integer equipWeaponCode, AverageStatistics merged) {
        super.merge(merged);

        Integer groupCode = WeaponSplitGroup.representativeOf(equipWeaponCode);

        findOrCreateWeapon(weaponNum, null).merge(merged);          // weaponNum 합산 (항상)
        if (groupCode != null) {
            findOrCreateWeapon(weaponNum, groupCode).merge(merged); // 대표 코드별 분리
        }
    }

    private WeaponResponse findOrCreateWeapon(Integer weaponNum, Integer equipWeaponCode) {
        for (WeaponResponse weapon : weaponList) {
            if (Objects.equals(weapon.getWeaponNum(), weaponNum)
                    && Objects.equals(weapon.getEquipWeaponCode(), equipWeaponCode)) {
                return weapon;
            }
        }

        WeaponResponse created = new WeaponResponse(weaponNum, equipWeaponCode);
        weaponList.add(created);
        return created;
    }
}
