package eternal_return.dayogg.statistics.dto.request.range;

import eternal_return.dayogg.tier.enums.TierEnum;
import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StatisticsRequestSide(
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
