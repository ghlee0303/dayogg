package eternal_return.dayogg.meta.service;

import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.core.api.ApiService;
import eternal_return.dayogg.meta.json.ArmorJson;
import eternal_return.dayogg.meta.json.WeaponJson;
import eternal_return.dayogg.meta.meta.item.EquipInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipService {

    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    private static final String WEAPON_API_PATH = "v2/data/ItemWeapon";
    private static final String ARMOR_API_PATH = "v2/data/ItemArmor";

    @ServiceLogging
    public List<EquipInfo> getEquips() {
        List<EquipInfo> equips = new ArrayList<>(getWeapon());
        equips.addAll(getArmor());

        return equips;
    }

    private List<EquipInfo> getWeapon() {
        List<WeaponJson> weapons = objectMapper.treeToValue(
                apiService.callApi(WEAPON_API_PATH).get("data"),
                new TypeReference<>() {}
        );

        return weapons.stream()
                .map(WeaponJson::toEquipInfo)
                .toList();
    }

    private List<EquipInfo> getArmor() {
        List<ArmorJson> armors = objectMapper.treeToValue(
                apiService.callApi(ARMOR_API_PATH).get("data"),
                new TypeReference<>() {}
        );

        return armors.stream()
                .map(ArmorJson::toEquipInfo)
                .toList();
    }
}
