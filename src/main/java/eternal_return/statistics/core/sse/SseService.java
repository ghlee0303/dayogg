package eternal_return.statistics.core.sse;

import eternal_return.statistics.core.sse.enums.SseStatus;
import eternal_return.statistics.core.idempotent.Idempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE(Server-Sent Events) 구독 및 이벤트 전송 서비스.
 *
 * <p>클라이언트가 {@code /sse/subscribe}에 접속하면 {@link SseEmitter}를 생성하여
 * {@code emitters} 맵에 등록한다. 이후 서버에서 발생하는 이벤트(처리 완료, 오류 등)를
 * 해당 emitter를 통해 실시간으로 전송한다.
 *
 * <p>동시성: {@link ConcurrentHashMap}을 사용해 멀티 쓰레드 환경에서 안전하게 emitter를 관리한다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

    /**
     * sseKey → SseEmitter 매핑 저장소.
     * 가상 쓰레드 환경에서 동시 접근이 발생하므로 ConcurrentHashMap을 사용한다.
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    @Value("${sse.timeout-minutes}")
    private int sseTimeOutMinutes;

    public SseEmitter subscribe(String sseKey) {
        SseEmitter emitter = new SseEmitter(sseTimeOutMinutes * 60 * 1000L);

        // 동일 sseKey로 이미 구독 중인 emitter가 있으면 먼저 완료 처리 후 새 emitter로 교체
        SseEmitter old = emitters.put(sseKey, emitter);
        if (old != null) {
            old.complete();
        }

        // emitter 생명주기 콜백 등록 — 어떤 경우든 맵에서 제거하여 메모리 누수 방지
        emitter.onCompletion(() -> emitters.remove(sseKey, emitter));
        emitter.onTimeout(() -> emitters.remove(sseKey, emitter));
        emitter.onError(e -> emitters.remove(sseKey, emitter));

        send(sseKey, SseStatus.OPEN, "OPEN");

        return emitter;
    }

    public void send(String sseKey, SseStatus event, Object data) {
        SseEmitter emitter = emitters.get(sseKey);
        if (emitter == null) return; // 아직 구독하지 않았거나 이미 종료된 경우

        if (data == null) {
            log.warn("[SSE-SEND] data is null, skip");
            return;
        }

        try {
            SseEmitter.SseEventBuilder builder =
                    SseEmitter.event().data(data);

            // event 타입이 있으면 이름을 설정 — 클라이언트에서 addEventListener로 구분 가능
            if (event != null) {
                builder.name(event.getName());
            }

            emitter.send(builder);

            if (SseStatus.OPEN != event) {
                log.info("[SSE-SEND] {} : {}",
                        event != null ? event : SseStatus.MESSAGE,
                        data
                );
            }

        } catch (Exception e) {
            // 전송 실패 시 emitter를 맵에서 제거하고 오류 완료 처리
            emitters.remove(sseKey, emitter);
            emitter.completeWithError(e);
            log.warn("[SSE-SEND] send failed for key={}: {}", sseKey, e.getMessage());
        }
    }

    public void remove(String sseKey) {
        SseEmitter emitter = emitters.remove(sseKey);
        if (emitter == null) return; // 타임아웃·오류·전송 실패로 이미 정리된 경우

        emitter.complete();
    }

    public void sendByIdempotent(Idempotent idempotent, SseStatus event, Object data) {
        // 동일 작업을 기다리는 모든 클라이언트에게 이벤트 전송
        for (String sseKey : idempotent.sseKeyList()) {
            send(sseKey, event, data);

            // 종단 이벤트(MESSAGE/ERROR) 전송 후 커넥션을 닫는다.
            // 완료하지 않으면 emitter 타임아웃까지 열린 채 남고,
            // Spring이 AsyncRequestTimeoutException으로 종료하며 WARN을 남긴다.
            remove(sseKey);
        }
    }
}