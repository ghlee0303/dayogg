package eternal_return.dayogg.meta.service;

import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.core.api.ApiService;
import eternal_return.dayogg.meta.meta.LocaleMeta;
import eternal_return.dayogg.meta.json.LocaleJson;
import eternal_return.dayogg.meta.enums.LanguageEnum;
import eternal_return.dayogg.meta.enums.LocaleEnum;
import eternal_return.dayogg.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocaleService {

    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    @ServiceLogging
    public LocaleMeta getL10n() {
        LocaleMeta localeMeta = new LocaleMeta();

        // static LocaleEnum
        for (LocaleEnum type : LocaleEnum.values()) {
            if (!type.isStatic()) continue;
            localeMeta.putByStaticLocale(type, loadStaticLocale(type));
        }

        // API LocaleEnum
        for (LanguageEnum language : LanguageEnum.values()) {
            String text = requestL10nText(language.getName());
            localeMeta.putByApiLocale(text, language);
        }

        return localeMeta;
    }

    private Map<String, LocaleJson> loadStaticLocale(LocaleEnum type) {
        return JsonUtils.readJsonFile(objectMapper, type.getPath(), new TypeReference<>() {});
    }

    private String requestL10nText(String language) {
        JsonNode meta = apiService.callApi("/v1/l10n/" + language);
        String l10nUrl = meta.get("data").get("l10Path").asString();

        return apiService.callDownloadApi(l10nUrl);
    }
}
