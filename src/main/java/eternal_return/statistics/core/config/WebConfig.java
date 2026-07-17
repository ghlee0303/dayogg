package eternal_return.statistics.core.config;

import eternal_return.statistics.common.utils.DateTimeUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                                      // 모든 API 경로
                .allowedOrigins("http://localhost:5173")                // 허용할 출처 (URL)
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
