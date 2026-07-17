package eternal_return.statistics.core.bucket;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BucketService {
    private final Bucket requestBucket;
    private final Bucket crawlingBucket;

    public void apiBucketBlocking() {
        consume(requestBucket);
    }

    public void crawlingBucketBlocking() {
        consume(crawlingBucket);
    }

    private void consume(Bucket bucket) {
        try {
            bucket.asBlocking().consume(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
