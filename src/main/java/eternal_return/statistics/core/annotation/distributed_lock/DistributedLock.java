package eternal_return.statistics.core.annotation.distributed_lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String value();
    long waitTime() default 5;
    long leaseTime() default 30;
}
