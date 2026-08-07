package eternal_return.dayogg.meta.meta.season;

import eternal_return.dayogg.common.utils.ListUtils;
import eternal_return.dayogg.core.exception.child.SeasonException;
import eternal_return.dayogg.core.exception.enums.LogMessageEnum;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
public class SeasonMeta {

    private final List<SeasonInfo> seasonMetaList;
    private final SeasonInfo nowSeason;

    public SeasonMeta(List<SeasonInfo> seasonMetaList) {
        this.seasonMetaList = seasonMetaList;
        this.nowSeason = resolveNowSeason(seasonMetaList);
    }

    private static SeasonInfo resolveNowSeason(List<SeasonInfo> seasonMetaList) {
        // 최신 시즌부터 역순으로 isCurrent 검색
        for (int i = seasonMetaList.size() - 1; i >= 0; i--) {
            SeasonInfo seasonInfo = seasonMetaList.get(i);
            if (!seasonInfo.isCurrent()) continue;
            return seasonInfo;
        }

        throw new SeasonException.NotFound(LogMessageEnum.NOT_FOUND.format("isCurrent true 인 시즌을 찾을 수 없음"));
    }

    public Optional<SeasonInfo> findByDateTimeRange(LocalDateTime keyDateTime) {
        return ListUtils.findByDateTimeRange(
                seasonMetaList,
                keyDateTime,
                SeasonInfo::startDate,
                SeasonInfo::endDate
        );
    }

    public SeasonInfo getByDateTimeRange(LocalDateTime keyDateTime) {
        return findByDateTimeRange(keyDateTime).orElseThrow(() ->
                new SeasonException.NotFound("can't find by dateTime")
                        .withContext("keyDateTime", keyDateTime)
        );
    }

    public SeasonInfo getById(Integer seasonId) {
        return ListUtils.findById(
                seasonMetaList,
                seasonId,
                SeasonInfo::seasonId
        ).orElseThrow(() ->
                new SeasonException.NotFound("can't find by seasonId")
                        .withContext("seasonId", seasonId)
        );
    }
}
