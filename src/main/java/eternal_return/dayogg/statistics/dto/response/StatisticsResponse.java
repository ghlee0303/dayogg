package eternal_return.dayogg.statistics.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eternal_return.dayogg.battle_result.BattleResult;
import eternal_return.dayogg.common.enums.RangeSideEnum;
import eternal_return.dayogg.common.utils.ListUtils;
import eternal_return.dayogg.player.dto.PlayerSeasonDto;
import eternal_return.dayogg.statistics.dto.request.range.StatisticsRequestSide;
import eternal_return.dayogg.statistics.dto.request.range.StatisticsRangeRequest;
import eternal_return.dayogg.statistics.extend.AverageStatistics;
import eternal_return.dayogg.tier.enums.TierEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StatisticsResponse.SeasonTotal.class, name = "SEASON_TOTAL"),
        @JsonSubTypes.Type(value = StatisticsResponse.Tier.class, name = "TIER"),
        @JsonSubTypes.Type(value = StatisticsResponse.Range.class, name = "RANGE"),
})
public abstract class StatisticsResponse extends AverageStatisticsResponse {
    protected final Type type;
    protected String label;
    protected LinkedList<CharacterStatisticsResponse> characterList;
    protected EnumMap<RangeSideEnum, StatisticsResponseSide> range;

    public enum Type {
        SEASON_TOTAL, TIER, RANGE
    }

    protected StatisticsResponse(Type type) {
        this.type = type;
        this.characterList = new LinkedList<>();
        this.range = StatisticsResponseSide.mapCreator();
    }

    @Override
    public void merge(BattleResult battleResult) {
        AverageStatistics merged = new AverageStatistics(battleResult);
        super.merge(merged);

        mergeCharacter(
                battleResult.getCharacterNum(),
                battleResult.getBestWeapon(),
                battleResult.getEquipments(),
                merged
        );

        StatisticsResponseSide.merge(
                this.range,
                battleResult.getMmrBefore(),
                battleResult.getStartDtm(),
                battleResult.getTierEnum()
        );
    }

    public void merge(StatisticsResponse merged) {
        super.merge(merged);
        StatisticsResponseSide.merge(this.range, merged.getRange());

        for (CharacterStatisticsResponse mergedCharacter : merged.characterList) {
            mergeCharacter(mergedCharacter);
        }
    }

    private void mergeCharacter(CharacterStatisticsResponse merged) {
        Optional<CharacterStatisticsResponse> optional = ListUtils.findById(characterList, merged.getCharacterNum(), CharacterStatisticsResponse::getCharacterNum);
        CharacterStatisticsResponse character;

        if (optional.isEmpty()) {
            character = new CharacterStatisticsResponse(merged.getCharacterNum());
            characterList.add(character);
        } else {
            character = optional.get();
        }

        character.merge(merged);
    }

    private void mergeCharacter(int characterNum, int weaponNum, List<Integer> equipments, AverageStatistics merged) {
        Optional<CharacterStatisticsResponse> optional = ListUtils.findById(characterList, characterNum, CharacterStatisticsResponse::getCharacterNum);
        CharacterStatisticsResponse character;

        if (optional.isEmpty()) {
            character = new CharacterStatisticsResponse(characterNum);
            characterList.add(character);
        } else {
            character = optional.get();
        }

        Integer equipWeaponCode = (equipments == null || equipments.isEmpty()) ? null : equipments.getFirst();
        character.merge(weaponNum, equipWeaponCode, merged);
    }

    @Getter
    public static class SeasonTotal extends StatisticsResponse {
        TierEnum tierEnum;
        Integer mmr;
        Integer totalRank;
        Integer serverRank;
        String serverName;
        Map<TierEnum, EnumMap<RangeSideEnum, StatisticsResponseSide>> totalRange;

        public SeasonTotal(
                PlayerSeasonDto playerSeason,
                Map<TierEnum, Tier> tierMap
        ) {
            super(Type.SEASON_TOTAL);
            this.label = Type.SEASON_TOTAL.name();
            this.tierEnum = playerSeason.tierEnum();
            this.mmr = playerSeason.mmr();
            this.totalRank = playerSeason.rank();
            this.serverRank = playerSeason.serverRank();
            this.serverName = playerSeason.serverEnum().getName();
            this.totalRange = new HashMap<>();

            for (TierEnum key : tierMap.keySet()) {
                Tier tier = tierMap.get(key);

                totalRange.put(key, tier.getRange());
            }
        }
    }

    @Getter
    public static class Tier extends StatisticsResponse {
        TierEnum tierEnum;
        EnumMap<RangeSideEnum, StatisticsRequestSide> request;

        public Tier(String label, TierEnum tierEnum) {
            super(Type.TIER);
            this.label = label;
            this.tierEnum = tierEnum;
        }

        public Tier(TierEnum tierEnum) {
            super(Type.TIER);
            this.label = tierEnum.name();
            this.tierEnum = tierEnum;
        }

        public Tier(StatisticsRangeRequest request, TierEnum tierEnum) {
            super(Type.TIER);
            this.label = request.label();
            this.tierEnum = tierEnum;
            this.request = request.range();
        }
    }

    @Getter
    public static class Range extends StatisticsResponse {
        EnumMap<RangeSideEnum, StatisticsRequestSide> request;

        public Range(String label) {
            super(Type.RANGE);
            this.label = label;
        }

        public Range(StatisticsRangeRequest request) {
            super(Type.RANGE);
            this.label = request.label();
            this.request = request.range();
        }
    }
}
