package eternal_return.statistics.meta.dto;

import eternal_return.statistics.tier.enums.TierEnum;

public record TierMetaResponse(
        TierEnum tierEnum, String name, String key
) {
}
