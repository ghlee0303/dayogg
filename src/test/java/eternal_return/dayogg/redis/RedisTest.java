package eternal_return.dayogg.redis;

import eternal_return.dayogg.core.idempotent.Idempotent;
import eternal_return.dayogg.core.idempotent.IdempotentRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class RedisTest {
    @Autowired
    private IdempotentRedisRepository idempotentRedisRepository;

    private static final String REDIS_KEY = "job-1";

    @Test
    void redis_저장_조회_삭제_통합테스트() {
//        // given
//        Idempotent idempotent = new Idempotent(
//                REDIS_KEY,
//                List.of("SSE-1", "SSE-2222")
//        );
//
//        idempotentRedisRepository.saveIdempotent(idempotent, null);
//
//        // then - 조회
//        Idempotent result = idempotentRedisRepository.getIdempotent(REDIS_KEY);
//
//        System.out.println(result);
    }

    @Test
    void redis_NullTest() {
//        // then - 조회
//        Idempotent result = idempotentRedisRepository.getIdempotent("REDIS_KEY");
//
//        System.out.println(result);
    }
}
