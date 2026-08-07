package eternal_return.dayogg.meta.service;

import eternal_return.dayogg.core.annotation.service_logging.LoggingType;
import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.core.api.ApiService;
import eternal_return.dayogg.meta.json.WeaponJson;
import eternal_return.dayogg.meta.meta.item.EquipInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeaponService {

    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    private static final String API_PATH = "v2/data/ItemWeapon";

    @ServiceLogging(loggingType = LoggingType.PARENT)
    public List<EquipInfo> getWeapon() {
        List<WeaponJson> weapons = objectMapper.treeToValue(
                apiService.callApi(API_PATH).get("data"),
                new TypeReference<>() {}
        );

        return weapons.stream()
                .map(WeaponJson::toEquipInfo)
                .toList();
    }
}
