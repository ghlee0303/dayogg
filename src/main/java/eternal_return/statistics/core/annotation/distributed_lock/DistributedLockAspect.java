package eternal_return.statistics.core.annotation.distributed_lock;

import eternal_return.statistics.core.annotation.SpelFacade;
import eternal_return.statistics.core.exception.BusinessException;
import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import eternal_return.statistics.core.exception.enums.LogMessageEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {
    private final RedissonClient redissonClient;
    private final SpelFacade spelFacade;

    private final String PREFIX = "lock:order:";

    @Around("@annotation(DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = spelFacade.getSingleValue(joinPoint, distributedLock::value, String.class);

        RLock lock = redissonClient.getLock(PREFIX + key);

        boolean acquired = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), TimeUnit.SECONDS);

        if (!acquired) {
            throw new BusinessException(ExceptionResponseEnum.TOO_MANY_REQUESTS, LogMessageEnum.TIMEOUT_DISTRIBUTED_LOCK.format());
        }

        try {
            return joinPoint.proceed();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
