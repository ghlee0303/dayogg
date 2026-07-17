package eternal_return.statistics.meta.json;

import eternal_return.statistics.meta.enums.LanguageEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class LocaleJson {
    private String key;
    private Map<LanguageEnum, String> locale;

    public LocaleJson(String key) {
        this.key = key;
        this.locale = new LinkedHashMap<>();
    }

    public void put(LanguageEnum key, String value) {
        locale.put(key, value);
    }

    public Map<LanguageEnum, String> toLanguageMap() {
        Map<LanguageEnum, String> result = new HashMap<>();

        for (LanguageEnum enums : LanguageEnum.values()) {
            result.put(enums, locale.get(enums));
        }

        return result;
    }
}
