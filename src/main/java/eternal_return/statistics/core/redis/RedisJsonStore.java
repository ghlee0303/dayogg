package eternal_return.statistics.core.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisJsonStore {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void save(String key, T value) {
        save(key, value, null);
    }

    public <T> void save(String key, T value, @Nullable Duration ttl) {
        String json = toJson(value);

        if (ttl == null) {
            redisTemplate.opsForValue().set(key, json);
        } else {
            redisTemplate.opsForValue().set(key, json, ttl);
        }
    }

    public void saveZSET(String key, String value, long score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public <T> T find(String key, Class<T> type) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return null;

        return fromJson(value, type);
    }

    public List<String> findZSET(
            String key, long start, long end, int offset, int count) {
        return redisTemplate
                .opsForZSet()
                .reverseRangeByScore(key, start, end, offset, count)
                .stream().toList();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalStateException(
                    "Redis value serialization failed"
            );
        }
    }

    private <T> T fromJson(String value, Class<T> type) {
        try {
            return objectMapper.readValue(value, type);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalStateException(
                    "Redis value deserialization failed"
            );
        }
    }
}
