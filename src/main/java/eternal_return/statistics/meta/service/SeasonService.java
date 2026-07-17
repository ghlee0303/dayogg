package eternal_return.statistics.meta.service;

import eternal_return.statistics.core.api.ApiService;
import eternal_return.statistics.meta.meta.season.SeasonInfo;
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

    public List<SeasonInfo> getSeason() {
        return objectMapper.treeToValue(
                apiService.callApi(API_PATH).get("data"),
                new TypeReference<>() {}
        );
    }
}