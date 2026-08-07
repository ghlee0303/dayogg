package eternal_return.dayogg.core.idempotent;

import eternal_return.dayogg.core.redis.RedisJsonStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IdempotentRedisRepository {

    private final RedisJsonStore redisJsonStore;
    private final String keyPrefix = "idempotency:";

    public void saveIdempotent(Idempotent idempotent, Duration ttl) {
        String redisKey = keyPrefix + idempotent.key();
        redisJsonStore.save(redisKey, idempotent, ttl);
    }

    public Idempotent getIdempotent(String key) {
        String redisKey = keyPrefix + key;
        return redisJsonStore.find(redisKey, Idempotent.class);
    }

    public void deleteIdempotent(String key) {
        String redisKey = keyPrefix + key;
        redisJsonStore.delete(redisKey);
    }
}