package eternal_return.dayogg.statistics.dto.response;

import eternal_return.dayogg.common.utils.ListUtils;
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

        public WeaponResponse(Integer weaponNum) {
            this.weaponNum = weaponNum;
        }
    }

    public CharacterStatisticsResponse(Integer characterNum) {
        this.characterNum = characterNum;
        this.weaponList = new ArrayList<>();
    }

    public void merge(CharacterStatisticsResponse merged) {
        for (WeaponResponse mergedWeapon : merged.weaponList) {
            merge(mergedWeapon.weaponNum, mergedWeapon);
        }
    }

    public void merge(int weaponNum, AverageStatistics merged) {
        super.merge(merged);

        Optional<WeaponResponse> optional = ListUtils.findById(weaponList, weaponNum, WeaponResponse::getWeaponNum);
        WeaponResponse weapon;

        if (optional.isEmpty()) {
            weapon = new WeaponResponse(weaponNum);
            weaponList.add(weapon);
        } else {
            weapon = optional.get();
        }

        weapon.merge(merged);
    }
}
