package eternal_return.dayogg.core.config;

import eternal_return.dayogg.common.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 허용할 프론트 출처. 콤마로 여러 개(예: http://localhost:5173,https://app.example.com).
    // 환경변수 CORS_ALLOWED_ORIGINS 로 운영 도메인 주입(application.yml 참고).
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                                      // 모든 API 경로
                .allowedOrigins(allowedOrigins)                         // 허용할 출처 (URL)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE") // HTTP 메서드 허용
                .allowedHeaders("*")                                    // 모든 헤더 허용
                .allowCredentials(true);                                // 쿠키 인증 요청 허용
    }

    /**
     * 쿼리 파라미터 등에서 String → LocalDateTime 변환을 확장.
     * 1) ISO_LOCAL_DATE_TIME (Spring 기본 호환)
     * 2) "yyyy-MM-dd" 또는 "yyyy-MM-dd HH:mm:ss" (시간 누락 시 00:00:00)
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDateTime.class, source -> {
            try {
                return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                return LocalDateTime.parse(source, DateTimeUtils.YMD_OR_YMD_HMS);
            }
        });
    }
}
