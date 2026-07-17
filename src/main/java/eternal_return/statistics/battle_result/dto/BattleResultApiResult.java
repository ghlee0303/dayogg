package eternal_return.statistics.battle_result.dto;

import eternal_return.statistics.battle_result.client.BattleResultApiResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class BattleResultApiResult {
    private int playerLevel;
    private List<BattleResultApiResponse> responseList = new ArrayList<>();

    public void add(BattleResultApiResponse response) {
        response.arrayCutting();
        response.parsingAfter();

        responseList.add(response);
    }

    public void updatePlayerInfo(BattleResultApiResponse response) {
        playerLevel = Math.max(playerLevel, response.getAccountLevel());
    }

    public List<Integer> getSeasonIdList() {
        Set<Integer> seasonIdSet = new HashSet<>();

        for (BattleResultApiResponse response : responseList) {
            seasonIdSet.add(response.getSeasonId());
        }

        return seasonIdSet.stream().toList();
    }

    public boolean isPresent() {
        return playerLevel > 0 && !responseList.isEmpty();
    }
}
