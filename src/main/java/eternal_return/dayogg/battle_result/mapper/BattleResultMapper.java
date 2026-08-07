package eternal_return.dayogg.battle_result.mapper;

import eternal_return.dayogg.battle_result.BattleResult;
import eternal_return.dayogg.battle_result.client.BattleResultApiResponse;
import eternal_return.dayogg.battle_result.dto.BattleResponse;
import eternal_return.dayogg.meta.meta.item.ItemMeta;
import eternal_return.dayogg.meta.meta.phase.PhaseDuration;
import eternal_return.dayogg.meta.meta.phase.PhaseMeta;
import eternal_return.dayogg.tier.enums.TierEnum;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BattleResultMapper {

    @Autowired
    protected ItemMeta itemMeta;

    @Autowired
    protected PhaseMeta phaseMeta;

    /**
     * API 응답을 엔티티로 변환하고, 메타데이터 기반 파생 필드까지 채워 반환한다.
     *
     * <p>Lombok {@code @Builder} + MapStruct 조합에서는 빌드된 타입을 {@code @MappingTarget}으로 받는
     * {@code @AfterMapping}이 호출되지 않으므로, 순수 매핑({@link #map})과 후처리를 명시적으로 분리한다.</p>
     */
    public BattleResult toEntity(BattleResultApiResponse response, Long playerId, TierEnum tierEnum) {
        BattleResult result = map(response, playerId, tierEnum);
        if (result == null) return null;

        // 구매 아이템 관련 필드: buyCamera/buyReconDrone/buyEmpDrone/buyViewItemCredits
        result.setUpBuyItems(
                itemMeta,
                toIntegerList(response.getItemTransferredConsole()),
                toIntegerList(response.getItemTransferredDrone())
        );

        // 페이즈 기반 필드: targetTimeVFCredits, isLateGame
        PhaseDuration phaseDuration = phaseMeta.resolve(response.getStartDtm());
        result.setUpTargetTimeVFCredit(phaseDuration.secondsUntil(2), phaseDuration.creditRegenMsAt(2));   // 1일차 밤 까지
        result.setUpLateGame(phaseDuration.secondsUntil(10));            // 5일차 밤 까지

        return result;
    }

    @Mapping(target = "playerId", source = "playerId")
    @Mapping(target = "tierEnum", source = "tierEnum")
    @Mapping(target = "gainCredits", source = "response.totalVFCredits")
    @Mapping(target = "sumGainCredits", source = "response.totalGainVFCredit")
    @Mapping(target = "bountyGainCredits", source = "response.killItemBountyGainVFCredit")
    @Mapping(target = "usedCredits", source = "response.usedVFCredits")
    @Mapping(target = "sumUsedCredits", source = "response.sumUsedVFCredits")
    @Mapping(target = "winDimensionRift", source = "response.winFromDimensionRift")
    @Mapping(target = "tacticalSkill", source = "response.tacticalSkillGroup")
    @Mapping(target = "equipments", source = "response.equipments")
    protected abstract BattleResult map(BattleResultApiResponse response, Long playerId, TierEnum tierEnum);

    /** 전투 결과 엔티티를 응답 DTO로 변환한다. */
    public abstract BattleResponse toResponse(BattleResult battleResult);

    /** {@code Integer[]} → {@code List<Integer>} 변환 */
    protected List<Integer> toIntegerList(Integer[] array) {
        return array == null ? null : new ArrayList<>(Arrays.asList(array));
    }
}
