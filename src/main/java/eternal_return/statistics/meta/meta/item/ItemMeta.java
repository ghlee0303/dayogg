package eternal_return.statistics.meta.meta.item;

import eternal_return.statistics.meta.enums.ItemEnum;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ItemMeta {

    private final Map<Integer, ItemInfo> codeMap;
    private final Map<Integer, EquipInfo> equipMap;

    public ItemMeta(Map<Integer, ItemInfo> source, List<EquipInfo> equips) {
        Map<Integer, ItemInfo> codeMap = new HashMap<>();
        Map<Integer, EquipInfo> equipMap = new HashMap<>();

        for (Map.Entry<Integer, ItemInfo> entry : source.entrySet()) {
            Integer code = entry.getKey();
            codeMap.put(code, entry.getValue().withCode(code));
        }

        for (EquipInfo equip : equips) {
            equipMap.put(equip.code(), equip);
        }

        this.codeMap = codeMap;
        this.equipMap = equipMap;
    }

    public ItemInfo getByCode(Integer code) {
        return codeMap.get(code);
    }

    public EquipInfo getEquipByCode(Integer code) {
        return equipMap.get(code);
    }

    public Map<Integer, ItemInfo> getByItemEnum(ItemEnum itemEnum) {
        Map<Integer, ItemInfo> result = new HashMap<>();

        for (Map.Entry<Integer, ItemInfo> entry : codeMap.entrySet()) {
            if (entry.getValue().itemEnum() != itemEnum) continue;
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}