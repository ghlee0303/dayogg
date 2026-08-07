package eternal_return.dayogg.meta.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eternal_return.dayogg.meta.enums.ItemGradeEnum;
import eternal_return.dayogg.meta.enums.WeaponEnum;
import eternal_return.dayogg.meta.meta.item.EquipInfo;

/**
 * {@code v2/data/ItemWeapon} 응답의 단일 무기 항목.
 *
 * <p>응답에는 전투 스탯 등 90여 개 필드가 오지만, 메타로 등록하는 값만 받는다.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeaponJson(
        Integer code,
        @JsonProperty("##Name") String name,
        String weaponType,
        ItemGradeEnum itemGrade
) {
    public EquipInfo toEquipInfo() {
        return new EquipInfo(code, itemGrade, WeaponEnum.fromApiName(weaponType));
    }
}
