package eternal_return.dayogg.common.utils;

import eternal_return.dayogg.common.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class JsonUtils {

    public static <T> T readJsonFile(ObjectMapper objectMapper, String path, TypeReference<T> typeReference) {
        try {
            ClassPathResource resource = new ClassPathResource(path);

            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, typeReference);
            }
        } catch (Exception e) {
            StructuredLog.error(log, "meta", e)
                    .addKeyValue("operation", "readJsonFile")
                    .addKeyValue("path", path)
                    .log("[META] json file read failed");
            throw new RuntimeException(e.getMessage());
        }
    }

    public static JsonNode readJsonFileToNode(ObjectMapper objectMapper, String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);

            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readTree(is);
            }
        } catch (Exception e) {
            StructuredLog.error(log, "meta", e)
                    .addKeyValue("operation", "readJsonFileToNode")
                    .addKeyValue("path", path)
                    .log("[META] json file read failed");
            throw new RuntimeException(e.getMessage());
        }
    }

    public static ArrayNode readArrayJsonFileToNode(ObjectMapper objectMapper, String path) throws IOException {
        JsonNode readNode = readJsonFileToNode(objectMapper, path);

        if (!readNode.isArray()) {
            throw new IllegalStateException("해당 JSON 파일은 Array 형식이 아닙니다. : " + path);
        }

        return (ArrayNode) readNode;
    }

    public static ObjectNode readObjectJsonFileToNode(ObjectMapper objectMapper, String path) throws IOException {
        JsonNode readNode = readJsonFileToNode(objectMapper, path);

        if (!readNode.isArray()) {
            throw new IllegalStateException("해당 JSON 파일은 Object 형식이 아닙니다. : " + path);
        }

        return (ObjectNode) readNode;
    }

    public static <T, K> Map<K, T> readArrayJsonFileToMap(
            ObjectMapper objectMapper,
            String path,
            Class<T> targetClass,
            Function<T, K> keyExtractor) throws IOException {
        JsonNode rootNode = JsonUtils.readJsonFileToNode(objectMapper, path);

        if (!rootNode.isArray()) {
            throw new IllegalStateException("JSON must be an array: " + path);
        }

        Map<K, T> resultMap = new HashMap<>();
        for (JsonNode node : rootNode) {
            T item = objectMapper.treeToValue(node, targetClass);
            resultMap.put(keyExtractor.apply(item), item);
        }

        return resultMap;
    }
}
