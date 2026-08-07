package eternal_return.dayogg.tier.top_tier_cut.service;

import eternal_return.dayogg.common.enums.MatchingMode;
import eternal_return.dayogg.meta.meta.season.SeasonMeta;
import eternal_return.dayogg.meta.meta.top_tier_cut.TopTierCutInfo;
import eternal_return.dayogg.core.api.ApiService;
import eternal_return.dayogg.tier.top_tier_cut.TopTierCut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * 외부 API에서 상위 티어 RP 커트라인을 수집하는 서비스.
 *
 * <p>API({@code v1/rank/top/{seasonId}/{matchingTeamMode}})에서 상위 랭킹 목록을 조회하여 300위(ETERNITY)와 1000위(DEMIGOD)의 MMR을 추출한다.
 * <br>API 값이 기본값보다 낮은 경우 {@code meta/default_value.json}의 기본값을 사용한다.
 */
@Service
@RequiredArgsConstructor
public class TopTierCutApiService {
    private final ApiService apiService;
    private final SeasonMeta seasonMeta;

    public Optional<TopTierCut> requestTopTierCut(TopTierCutInfo topTierCutMeta) {
        JsonNode resultNode = apiService.callApi(buildUri());
        JsonNode topRankList = resultNode.get("topRanks");

        if (topRankList == null) return Optional.empty();

        int demiGodMmrCut = topTierCutMeta.demiGodCut();
        int eternityMmrCut = topTierCutMeta.eternityCut();

        for (JsonNode playerNode : topRankList) {
            int rank = playerNode.get("rank").asInt();
            int mmr = playerNode.get("mmr").asInt();

            if (topTierCutMeta.isEternity(rank)) {
                eternityMmrCut = Math.max(mmr, eternityMmrCut);
            } else if (topTierCutMeta.isDemiGod(rank)) {
                demiGodMmrCut = Math.max(mmr, demiGodMmrCut);
            }
        }

        if (topTierCutMeta.isSame(demiGodMmrCut, eternityMmrCut)) return Optional.empty();

        return Optional.of(new TopTierCut(demiGodMmrCut, eternityMmrCut));
    }

    /**
     * 현재 시즌·랭크 모드에 맞는 랭킹 조회 URI를 생성한다.
     */
    private String buildUri() {
        return UriComponentsBuilder
                .fromPath("v1/rank/top/{seasonId}/{matchingTeamMode}")
                .buildAndExpand(seasonMeta.getNowSeason().seasonId(), MatchingMode.RANK.getCode())
                .toUriString();
    }
}