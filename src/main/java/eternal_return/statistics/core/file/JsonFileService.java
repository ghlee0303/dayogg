package eternal_return.statistics.core.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

@Service
@RequiredArgsConstructor
public class JsonFileService {
    private final ObjectMapper objectMapper;
    private final String jsonDir = "json/";

    public void saveJsonFile(String jsonString, String name) {
        String basePath = resolveBasePath();  // 환경에 따라 자동 결정
        String filePath = basePath + jsonDir + name;
        Object json = objectMapper.readValue(jsonString, Object.class);

        // json/ 디렉토리 없으면 생성
        new File(basePath + jsonDir).mkdirs();

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filePath), json);
    }

    private String resolveBasePath() {
        boolean isTest = ClassUtils.isPresent(
                "org.junit.jupiter.api.Test",
                getClass().getClassLoader()
        );

//        String projectRoot = System.getProperty("user.dir");
        return isTest
                ? "src/test/resources/"
                : "src/main/resources/";
    }

}
