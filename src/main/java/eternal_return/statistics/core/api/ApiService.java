package eternal_return.statistics.core.api;

import eternal_return.statistics.common.log.LogContext;
import eternal_return.statistics.core.bucket.BucketService;
import eternal_return.statistics.core.exception.BusinessException;
import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import eternal_return.statistics.core.exception.enums.LogMessageEnum;
import eternal_return.statistics.core.file.JsonFileService;
import eternal_return.statistics.common.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

    private final RestClient restClient;
    private final BucketService bucketService;
    private final ObjectMapper objectMapper;
    private final JsonFileService jsonFileService;

    @Value("${path.error-log}")
    private String errorLogPath;

    public JsonNode callApi(String uri) {
        long start = System.currentTimeMillis();

        JsonNode apiResultNode = objectMapper.readTree(bucketRequest(uri));
        int status = apiResultNode.get("code").asInt();

        switch (status) {
            case 400, 500 -> throw apiExceptionSave(apiResultNode, uri, status, ExceptionResponseEnum.SERVER_ERROR);
            case 403, 429 -> throw apiExceptionSave(apiResultNode, uri, status, ExceptionResponseEnum.TOO_MANY_REQUESTS);
        }

        long elapsed = System.currentTimeMillis() - start;
        LogContext.addLong("apiMillis", elapsed);
        LogContext.addLong("apiCount", 1L);

        return apiResultNode;
    }

    public String callDownloadApi(String uri) {
        byte[] bytes = RestClient.create()
                .get()
                .uri(uri)
                .retrieve()
                .body(byte[].class);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String bucketRequest(String uri) {
        // 외부 API가 간헐적으로 null 본문을 반환할 때가 있어, 최초 호출 포함 최대 3회(= 2회 재실행) 시도한다.
        for (int attempt = 1; attempt <= 3; attempt++) {
            bucketService.apiBucketBlocking();

            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (response != null) {
                return response;
            }

            log.warn("[API] null response | attempt {}/3 | uri: {}", attempt, uri);
        }

        throw new BusinessException(ExceptionResponseEnum.SERVER_ERROR, "API", LogMessageEnum.NULL_VALUE.format(uri));
    }

    private BusinessException apiExceptionSave(
            JsonNode apiResultNode, String uri, int status,
            ExceptionResponseEnum exceptionResponseEnum
    ) {
        String fileName = saveErrorJson(apiResultNode, uri, status);
        return new BusinessException(exceptionResponseEnum, "API", LogMessageEnum.API_BAD_STATUS.format(status, fileName));
    }

    private String saveErrorJson(JsonNode apiResultNode, String uri, int status) {
        String now = DateTimeUtils.fromLocalDateTime(LocalDateTime.now());

        // 원본 노드를 변조하지 않도록 새 ObjectNode에 필드를 복사
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.setAll((ObjectNode) apiResultNode);
        objectNode.put("dateTime", now);
        objectNode.put("uri", uri);
        objectNode.put("status", status);

        String fileName = status + "_" + now;
        jsonFileService.saveJsonFile(objectNode.asString(), errorLogPath + fileName);

        return fileName;
    }
}