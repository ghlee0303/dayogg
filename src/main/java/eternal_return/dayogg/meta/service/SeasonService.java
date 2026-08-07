package eternal_return.dayogg.meta.service;

import eternal_return.dayogg.core.annotation.service_logging.LoggingType;
import eternal_return.dayogg.core.annotation.service_logging.ServiceLogging;
import eternal_return.dayogg.core.api.ApiService;
import eternal_return.dayogg.meta.meta.season.SeasonInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonService {

    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    private static final String API_PATH = "v2/data/Season";

    @ServiceLogging(loggingType = LoggingType.PARENT)
    public List<SeasonInfo> getSeason() {
        return objectMapper.treeToValue(
                apiService.callApi(API_PATH).get("data"),
                new TypeReference<>() {}
        );
    }
}