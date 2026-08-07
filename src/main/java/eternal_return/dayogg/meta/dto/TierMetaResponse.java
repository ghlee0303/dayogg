package eternal_return.dayogg.meta.dto;

import eternal_return.dayogg.tier.enums.TierEnum;

public record TierMetaResponse(
        TierEnum tierEnum, String name, String key
) {
}
