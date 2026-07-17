package eternal_return.statistics.core.api;

import eternal_return.statistics.core.bucket.BucketService;
import eternal_return.statistics.core.exception.BusinessException;
import eternal_return.statistics.core.exception.enums.ExceptionResponseEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlingService {

    private static final int TIMEOUT_MS = 10_000;
    private static final String USER_AGENT = "Mozilla/5.0";

    private final BucketService bucketService;

    public Document fetchDocument(String url) {
        bucketService.crawlingBucketBlocking();

        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();
        } catch (IOException e) {
            log.error("[CrawlingService] fetch failed url={}", url, e);
            throw new BusinessException(ExceptionResponseEnum.SERVER_ERROR, "CRAWLING", "fetch failed: " + url);
        }
    }

    public Elements select(String url, String cssQuery) {
        return fetchDocument(url).select(cssQuery);
    }

    public String selectText(String url, String cssQuery) {
        Element element = fetchDocument(url).selectFirst(cssQuery);
        return element != null ? element.text() : "";
    }
}