package eternal_return.statistics.core.sse;

import eternal_return.statistics.core.exception.BusinessException;
import eternal_return.statistics.core.exception.ErrorResponse;
import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import eternal_return.statistics.core.idempotent.Idempotent;
import eternal_return.statistics.core.idempotent.IdempotentService;
import eternal_return.statistics.core.sse.enums.SseStatus;
import eternal_return.statistics.core.sse.exception.SseJobException;
import eternal_return.statistics.core.thread.ThreadExecutor;
import eternal_return.statistics.core.thread.exception.ThreadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * SSE 비동기 잡 실행기.
 *
 * <p>Controller에서 넘어온 작업(Supplier)을 가상 쓰레드에서 실행하고,
 * 완료·오류 결과를 SSE로 클라이언트에 전송한다.
 * 멱등성(Idempotent) 관리도 함께 담당하여 동일 요청이 중복 실행되지 않도록 한다.
 *
 * <p>흐름 요약:
 * <pre>
 *   Controller
 *     → controllerJobStart()  : 멱등 키 검사 후 가상 쓰레드 제출
 *       → serviceThreadJobStart() : 실제 작업 실행 + SSE 응답 전송
 * </pre>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SseJobDispatcher {

    /** 잡 단위 추적 축. 요청의 {@code code} 는 잡 쓰레드까지 1:1로 이어지지 않는다. */
    private static final String JOB_KEY = "idempotentKey";

    private final IdempotentService idempotentService;
    private final SseService sseService;
    private final ThreadExecutor threadExecutor;

    public <T> SseEmitter controllerJobStart(String idempotentKey, Supplier<T> task) {
        // 요청 로그에 잡의 식별자를 남긴다. 요청 로그의 code → idempotentKey → 잡 로그 순으로 추적한다.
        // (MDC 정리는 ControllerLoggingAspect 가 요청 종료 시 일괄 복원한다)
        MDC.put(JOB_KEY, idempotentKey);

        String sseKey = UUID.randomUUID().toString();
        SseEmitter sseEmitter = sseService.subscribe(sseKey);

        // 이미 진행 중인 동일 작업이 있으면 현재 sseKey만 합류시키고 조기 반환.
        // 없으면 idempotent를 생성하고 아래에서 작업을 제출한다. (조회+생성은 단일 락 구간에서 원자적으로 수행)
        boolean joined;
        try {
            joined = idempotentService.joinOrCreate(idempotentKey, sseKey);

        } catch (RuntimeException e) {
            // 락 획득 실패 등으로 등록에 실패한 경우 emitter를 정리한다.
            // 아직 컨트롤러로 반환되기 전이라 async가 시작되지 않았고, 따라서 타임아웃 콜백도 돌지 않는다.
            // 여기서 치우지 않으면 emitters 맵에 영구히 남는다.
            // idempotent는 진행 중인 다른 작업의 것일 수 있으므로 삭제하지 않는다.
            sseService.remove(sseKey);

            throw e;
        }

        if (joined) {
            return sseEmitter;
        }

        try {
            // 가상 쓰레드에 작업 제출 — 논블로킹으로 즉시 반환
            threadExecutor.submit(() -> serviceThreadJobStart(idempotentKey, sseKey, task));

        } catch (ThreadTimeoutException e) {
            // submit 실패 시 정리 — task가 실행되지 않으므로 여기서 처리.
            // emitter를 먼저 치운다: deleteIdempotent가 락 획득 실패로 던지면 그 뒤 remove가 실행되지 않아
            // async 시작 전 emitter가 emitters 맵에 영구히 남기 때문이다.
            sseService.remove(sseKey);
            idempotentService.deleteIdempotent(idempotentKey);

            throw new SseJobException(ExceptionResponseEnum.TOO_MANY_REQUESTS, e.getMessage());
        } catch (Exception e) {
            // submit 실패 시 정리 — remove 먼저(위 ThreadTimeoutException 주석 참고)
            sseService.remove(sseKey);
            idempotentService.deleteIdempotent(idempotentKey);

            throw new SseJobException(ExceptionResponseEnum.SERVER_ERROR, e);
        }

        return sseEmitter;
    }

    /**
     * 실제 작업을 실행하고 결과를 SSE로 전송하는 공통 로직.
     *
     * <ul>
     *   <li>정상 완료: 결과를 대기 중인 모든 클라이언트에게 브로드캐스트한다.</li>
     *   <li>{@link BusinessException}: 예상된 비즈니스 오류 — 오류 메시지를 SSE로 전송한다.</li>
     *   <li>그 외 예외: 예상치 못한 서버 오류 — 로그 기록 후 일반 오류 메시지를 전송한다.</li>
     * </ul>
     *
     * <p>idempotent 삭제는 종단 이벤트 전송과 함께 {@link IdempotentService#finish}의
     * 락 구간 안에서 이루어진다.
     *
     * @param idempotentKey Redis 멱등 키
     * @param sseKey        최초 요청자의 sseKey — idempotent 조회 실패 시 폴백 대상
     * @param task          실행할 비즈니스 로직
     * @param <T>           작업 결과 타입
     */
    private <T> void serviceThreadJobStart(
            String idempotentKey,
            String sseKey,
            Supplier<T> task
    ) {
        // 요청 쪽 MDC(code 등)가 가상 쓰레드로 상속돼 따라올 수 있다.
        // 요청 N개가 잡 1개를 공유하므로(joinOrCreate) 특정 요청의 code 를 잡에 붙이면 오해를 부른다.
        // 잡의 추적 축은 idempotentKey 하나로 세운다.
        MDC.clear();
        MDC.put(JOB_KEY, idempotentKey);

        try {
            T result = task.get();
            sendToAll(idempotentKey, sseKey, SseStatus.MESSAGE, result);

        } catch (BusinessException e) {
            // 예상된 비즈니스 예외 — 클라이언트에 오류 내용 전달
            sendToAll(idempotentKey, sseKey, SseStatus.ERROR, ErrorResponse.fromBusiness(e));

        } catch (Exception e) {
            // 예상치 못한 예외 — 서버 로그에 스택트레이스 기록
            log.atError()
                    .addKeyValue("layer", "sse")
                    .setCause(e)
                    .log("[SSE] unexpected error");
            sendToAll(idempotentKey, sseKey, SseStatus.ERROR, ErrorResponse.unexpected("처리 중 오류가 발생했습니다."));

        } finally {
            MDC.clear();
        }
    }

    private void sendToAll(String idempotentKey, String fallbackSseKey, SseStatus event, Object data) {
        try {
            Idempotent idempotent = idempotentService.finish(idempotentKey);
            sseService.sendByIdempotent(idempotent, event, data);
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("layer", "sse")
                    .addKeyValue("sseKey", fallbackSseKey)
                    .setCause(e)
                    .log("[SSE] finish failed, fallback to sseKey");
            sseService.send(fallbackSseKey, event, data);
            sseService.remove(fallbackSseKey);
        }
    }
}