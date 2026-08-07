package eternal_return.dayogg.core.bucket;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BucketConfig {
    @Value("${api.bucket.limits}")
    private int API_BUCKET_LIMITS;

    @Bean
    public Bucket requestBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(API_BUCKET_LIMITS)
                                .refillIntervally(API_BUCKET_LIMITS, Duration.ofSeconds(1))
                                .build()
                )
                .build();
    }

    @Bean
    public Bucket crawlingBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(1)
                                .refillIntervally(1, Duration.ofSeconds(2))
                                .build()
                )
                .build();
    }
}
