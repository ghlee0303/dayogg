package eternal_return.statistics.battle_result.dto.request.range;

import eternal_return.statistics.tier.enums.TierEnum;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BattleRequestSide(
        TierEnum tierEnum,
        @Nullable Integer mmr,
        @Nullable LocalDate date
) {
    @Nullable
    public LocalDateTime dateTime() {
        if (date == null) return null;

        return date.atStartOfDay();
    }
}