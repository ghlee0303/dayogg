package eternal_return.statistics.common.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 기동 완료를 구조화 로그 한 줄로 남긴다.
 *
 * <p>Spring 기본 기동 로그 3줄("Starting…"·"No active profile set"·"Started … in Xs")은
 * {@code spring.main.log-startup-info: false} 로 꺼져 있다. 로거 이름이
 * {@code eternal_return.statistics.StatisticsApplication} 이라 {@code root: warn} 에 안 걸리고,
 * 값이 message 안에 박힌 평문이라 질의도 안 되기 때문이다.
 *
 * <p>대신 재시작 신호를 여기서 필드로 남긴다 — CloudWatch 에서 이 줄이 곧 배포·재기동 시점이다.
 * 매 이벤트에서 뺀 {@code service.version} 도 여기 한 줄에만 싣는다 (어느 빌드가 도는지는 알아야 한다).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StartupLogger {

    private static final String LAYER = "lifecycle";
    private static final String DEFAULT_PROFILE = "default";
    private static final String UNKNOWN_VERSION = "unknown";

    private final Environment environment;

    @EventListener
    public void onReady(ApplicationReadyEvent event) {
        StructuredLog.info(log, LAYER)
                .addKeyValue("startupMs", event.getTimeTaken().toMillis())
                .addKeyValue("profile", activeProfiles())
                .addKeyValue("version", environment.getProperty("spring.application.version", UNKNOWN_VERSION))
                .log("[STARTUP] ready");
    }

    /** 프로파일을 안 켜면 Spring 이 빈 배열을 주므로 {@code default} 로 메운다. */
    private String activeProfiles() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? DEFAULT_PROFILE : String.join(",", profiles);
    }
}
