package eternal_return.statistics.meta.meta.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import eternal_return.statistics.meta.enums.EquipCategory;
import eternal_return.statistics.meta.enums.EquipType;
import eternal_return.statistics.meta.enums.ItemGradeEnum;

/**
 * 장비(무기 / 방어구) 메타.
 *
 * <p>
 * {@code equipType}은 무기({@code WeaponEnum}) 또는 방어구({@code ArmorEnum})이며,
 * 대분류는 {@link #category()}로 구분한다.
 * </p>
 */
public record EquipInfo(
        Integer code,
        ItemGradeEnum itemGrade,
        EquipType equipType
) {
    /**
     * 무기·방어구 대분류. {@code equipType}이 어느 쪽인지 소비 측(프론트)이 구분할 수 있도록 응답에 함께 내려준다.
     */
    @JsonProperty("category")
    public EquipCategory category() {
        return equipType.category();
    }
}
