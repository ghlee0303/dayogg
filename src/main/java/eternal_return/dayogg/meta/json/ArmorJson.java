package eternal_return.dayogg.meta.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eternal_return.dayogg.meta.enums.ArmorEnum;
import eternal_return.dayogg.meta.enums.ItemGradeEnum;
import eternal_return.dayogg.meta.meta.item.EquipInfo;

/**
 * {@code meta/armor.json}의 단일 방어구 항목.
 *
 * <p>파일에는 전투 스탯 등 90여 개 필드가 있지만, 메타로 등록하는 값만 받는다.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArmorJson(
        Integer code,
        @JsonProperty("##Name") String name,
        ArmorEnum armorType,
        ItemGradeEnum itemGrade
) {
    public EquipInfo toEquipInfo() {
        return new EquipInfo(code, itemGrade, armorType);
    }
}
