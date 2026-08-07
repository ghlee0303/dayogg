package eternal_return.dayogg.core.idempotent;

import eternal_return.dayogg.core.annotation.distributed_lock.DistributedLock;
import eternal_return.dayogg.core.exception.enums.ExceptionResponseEnum;
import eternal_return.dayogg.core.exception.enums.LogMessageEnum;
import eternal_return.dayogg.core.sse.exception.SseJobException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class IdempotentService {

    private final IdempotentRedisRepository idempotentRedisRepository;

    @Value("${sse.timeout-minutes}")
    private long sseTimeoutMinutes;

    @DistributedLock(value = "#key", leaseTime = 60)
    public Idempotent finish(String key) {
        Idempotent idempotent = idempotentRedisRepository.getIdempotent(key);

        if (idempotent == null) {
            throw new SseJobException(ExceptionResponseEnum.SERVER_ERROR, LogMessageEnum.NULL_IDEMPOTENT.format(key));
        }

        idempotentRedisRepository.deleteIdempotent(key);
        return idempotent;
    }

    /**
     * 진행 중인 작업이 있으면 sseKey를 합류시키고, 없으면 새 Idempotent를 생성한다.
     *
     * <p>조회와 생성이 하나의 락 구간 안에 있어야 한다. 두 메서드로 나누면 그 사이에 락이 풀려,
     * 동시 요청이 모두 "작업 없음"으로 판단하고 각자 Idempotent를 생성(= 서로 덮어쓰기)하면서
     * 중복 실행되고 먼저 등록된 sseKey가 유실된다.
     *
     * @param newJobId 새로 생성할 경우 부여할 jobId. 합류하면 쓰이지 않는다.
     * @return 이 요청이 붙은 작업의 jobId — 새로 만들었으면 {@code newJobId}, 합류했으면 진행 중인 작업의 것.
     *         호출부는 둘을 비교해 합류 여부를 판단한다.
     */
    @DistributedLock(value = "#key")
    public String joinOrCreate(String key, String sseKey, String newJobId) {
        Idempotent idempotent = idempotentRedisRepository.getIdempotent(key);

        if (idempotent == null) {
            idempotentRedisRepository.saveIdempotent(
                    Idempotent.create(key, newJobId, sseKey), Duration.ofMinutes(sseTimeoutMinutes));

            return newJobId;
        }

        idempotent.addSseKey(sseKey);
        idempotentRedisRepository.saveIdempotent(idempotent, Duration.ofMinutes(sseTimeoutMinutes));

        return idempotent.jobId();
    }

    @DistributedLock(value = "#key")
    public void deleteIdempotent(String key) {
        idempotentRedisRepository.deleteIdempotent(key);
    }
}