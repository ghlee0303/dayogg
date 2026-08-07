package eternal_return.dayogg.core.api;

import eternal_return.dayogg.common.log.LogContext;
import eternal_return.dayogg.common.log.StructuredLog;
import eternal_return.dayogg.core.bucket.BucketService;
import eternal_return.dayogg.core.exception.BusinessException;
import eternal_return.dayogg.core.exception.enums.ExceptionResponseEnum;
import eternal_return.dayogg.core.exception.enums.LogMessageEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

    /** {@code error.payload} 상한 (4KB) */
    private static final int PAYLOAD_MAX_LENGTH = 4096;

    private final RestClient restClient;
    private final BucketService bucketService;
    private final ObjectMapper objectMapper;

    public JsonNode callApi(String uri) {
        long start = System.currentTimeMillis();

        JsonNode apiResultNode = objectMapper.readTree(bucketRequest(uri));
        int status = apiResultNode.get("code").asInt();

        switch (status) {
            case 400, 500 -> throw apiBadStatus(apiResultNode, uri, status, ExceptionResponseEnum.SERVER_ERROR);
            case 403, 429 -> throw apiBadStatus(apiResultNode, uri, status, ExceptionResponseEnum.TOO_MANY_REQUESTS);
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

            StructuredLog.warn(log, "api")
                    .addKeyValue("attempt", attempt)
                    .addKeyValue("http.uri", uri)
                    .log("[API] null response");
        }

        throw new BusinessException(ExceptionResponseEnum.SERVER_ERROR, "API", LogMessageEnum.NULL_VALUE.format(uri));
    }

    private BusinessException apiBadStatus(
            JsonNode apiResultNode, String uri, int status,
            ExceptionResponseEnum exceptionResponseEnum
    ) {
        StructuredLog.warn(log, "api")
                .addKeyValue("http.uri", uri)
                .addKeyValue("http.status", status)
                .addKeyValue("error.payload", truncatePayload(objectMapper.writeValueAsString(apiResultNode)))
                .log("[API] bad status");

        return new BusinessException(exceptionResponseEnum, "API", LogMessageEnum.API_BAD_STATUS.format(status));
    }

    /**
     * CloudWatch 는 이벤트 하나의 크기에 상한이 있고 수집량이 곧 비용이라, 응답 본문을 잘라서 싣는다.
     * 원인 파악에는 앞부분이면 충분하다.
     */
    private String truncatePayload(String payload) {
        return payload.length() <= PAYLOAD_MAX_LENGTH
                ? payload
                : payload.substring(0, PAYLOAD_MAX_LENGTH) + "...(truncated)";
    }
}