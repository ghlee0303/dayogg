package eternal_return.statistics.meta.meta;

import eternal_return.statistics.meta.json.LocaleJson;
import eternal_return.statistics.meta.enums.LanguageEnum;
import eternal_return.statistics.meta.enums.LocaleEnum;
import lombok.Getter;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class LocaleMeta {

    private static final String SEPARATOR = "┃";
    private final Map<LanguageEnum, Map<LocaleEnum, Map<String, String>>> map;

    public LocaleMeta() {
        this.map = new EnumMap<>(LanguageEnum.class);
    }

    public void putByStaticLocale(LocaleEnum localeEnum, Map<String, LocaleJson> staticLocale) {
        for (String key : staticLocale.keySet()) {
            LocaleJson localeJson = staticLocale.get(key);
            Map<LanguageEnum, String> fromLocaleJson = localeJson.toLanguageMap();

            for (LanguageEnum languageEnum : fromLocaleJson.keySet()) {
                put(languageEnum, localeEnum, key, fromLocaleJson.get(languageEnum));
            }
        }
    }

    public void putByApiLocale(String text, LanguageEnum languageEnum) {
        if (text == null) return;

        for (String line : text.split("\n")) {
            if (!line.contains(SEPARATOR)) continue;
            String[] parts = line.split(SEPARATOR, 2);
            putByLine(parts, languageEnum);
        }
    }

    private void putByLine(String[] parts, LanguageEnum languageEnum) {
        String entryKey = parts[0];
        String value = parts[1].stripTrailing();

        int lastSlash = entryKey.lastIndexOf('/');
        if (lastSlash < 0) {
            putByEnumConstant(entryKey, value, languageEnum);
        } else {
            putByPrefix(entryKey, lastSlash, value, languageEnum);
        }
    }

    private void putByEnumConstant(String entryKey, String value, LanguageEnum languageEnum) {
        LocaleEnum localeEnum = LocaleEnum.getByEnumConstant(entryKey);
        if (localeEnum == null) return;

        String key = localeEnum.resolveKey(entryKey);
        if (key == null) return;

        put(languageEnum, localeEnum, key, value);
    }

    private void putByPrefix(String entryKey, int lastSlash, String value, LanguageEnum languageEnum) {
        String prefix = entryKey.substring(0, lastSlash + 1);
        LocaleEnum localeEnum = LocaleEnum.getByPrefix(prefix);
        if (localeEnum == null) return;

        String token = entryKey.substring(lastSlash + 1);
        String key = localeEnum.resolveKey(token);
        if (key == null) return;

        put(languageEnum, localeEnum, key, value);
    }

    private void put(LanguageEnum firstKey, LocaleEnum secondKey, String key, String value) {
        map.computeIfAbsent(firstKey, k -> new LinkedHashMap<>())
                .computeIfAbsent(secondKey, k -> new LinkedHashMap<>())
                .put(key, value);
    }

    public Map<LocaleEnum, Map<String, String>> getByLanguage(LanguageEnum languageEnum) {
        return map.get(languageEnum);
    }
}
