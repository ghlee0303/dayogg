package eternal_return.dayogg.route_auth.service;

import eternal_return.dayogg.core.api.CrawlingService;
import eternal_return.dayogg.route_auth.client.RouteApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteApiService {

    private static final String ROUTE_URL = "https://dak.gg/er/routes/";
    private static final String PLAYER_LINK_SELECTOR = "a[href^=/er/players/]";

    private final CrawlingService crawlingService;

    public Optional<RouteApiResponse> requestRoute(String routeId) {
        Document doc = crawlingService.fetchDocument(ROUTE_URL + routeId);

        Element playerLink = doc.selectFirst(PLAYER_LINK_SELECTOR);
        if (playerLink == null) {
            log.warn("route player link not found: {}", routeId);
            return Optional.empty();
        }

        Element titleNode = findClosestStrong(playerLink);
        if (titleNode == null) {
            log.warn("route title not found: {}", routeId);
            return Optional.empty();
        }

        return Optional.of(new RouteApiResponse(titleNode.text(), playerLink.text()));
    }

    private Element findClosestStrong(Element from) {
        Element ancestor = from.parent();
        while (ancestor != null) {
            Element strong = ancestor.selectFirst("strong");
            if (strong != null) return strong;
            ancestor = ancestor.parent();
        }
        return null;
    }
}
