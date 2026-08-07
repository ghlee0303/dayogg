package eternal_return.dayogg.core.annotation.service_logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLogging {
    String[] targetKeyList() default {};
    LoggingType loggingType() default LoggingType.CHILD;
}