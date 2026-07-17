package eternal_return.statistics.core.exception.enums;

import eternal_return.statistics.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogMessageEnum {
    NOT_FOUND("Not Found: {}"),
    NULL_VALUE("value null: {}"),
    API_BAD_STATUS("api bad {} | report file : {}"),
    TIMEOUT_THREAD("thread timeout | wait count: {}"),
    TIMEOUT_DISTRIBUTED_LOCK("distributed lock timeout: {}"),
    NULL_IDEMPOTENT("idempotent null | key: {}"),
    DUPLICATE_IDEMPOTENT("idempotent duplicate | key: {}"),
    RANGE_OUT("range out: {} | value: {}"),
    DUPLICATE_ROUTE_AUTH("route auth duplicate | routeId: {} | playerId: {}"),
    ;
    private final String format;

    public String format(Object... args) {
        return StringUtils.format(this.format, args);
    }
}