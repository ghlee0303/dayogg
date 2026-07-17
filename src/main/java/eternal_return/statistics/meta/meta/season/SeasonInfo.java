package eternal_return.statistics.meta.meta.season;

import com.fasterxml.jackson.annotation.JsonCreator;
import eternal_return.statistics.common.utils.DateTimeUtils;
import eternal_return.statistics.common.utils.StringUtils;

import java.time.LocalDateTime;

public record SeasonInfo(
        Integer seasonId, boolean isCurrent, LocalDateTime startDate, LocalDateTime endDate, boolean isPreSeason
) {
    @JsonCreator
    public SeasonInfo(
            Integer seasonID, boolean isCurrent, String seasonStart, String seasonEnd, String seasonName
    ) {
        this(seasonID, isCurrent,
                DateTimeUtils.toLocal(seasonStart),
                DateTimeUtils.toLocal(seasonEnd),
                StringUtils.contains(seasonName, "Pre-")
        );
    }
}
