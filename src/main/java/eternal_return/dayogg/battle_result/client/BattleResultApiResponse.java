package eternal_return.dayogg.battle_result.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import eternal_return.dayogg.common.enums.MatchingMode;
import eternal_return.dayogg.common.enums.ServerEnum;
import eternal_return.dayogg.common.utils.deserializer.LocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BattleResultApiResponse {
    /** 장비 슬롯 수 (무기, 옷, 머리, 팔, 다리) */
    private static final int EQUIPMENT_SLOT_SIZE = 5;

    private Integer gameId;
    private Integer seasonId;
    private Integer accountLevel;           // 플레이어 레벨

    private MatchingMode matchingMode;      // 매칭 모드 (코드 → Enum 변환)
    private ServerEnum serverEnum;          // 서버 (문자열 → Enum 변환)

    private Integer characterNum;
    private Integer skinCode;              // 스킨
    private Integer characterLevel;        // 레벨
    private Integer bestWeapon;           // 주 사용 무기
    private Integer bestWeaponLevel;      // 무기 숙련도

    private Integer gameRank;             // 최종 순위
    private Integer teamNumber;           // 팀 번호
    private Integer teamKill;             // TK
    private Integer monsterKill;          // 야동 킬
    private Integer viewContribution;     // 시야 점수
    private Integer preMade;              // 사전 큐 팀원 수

    private Integer mmrBefore;            // 게임 전 랭크 점수
    private Integer mmrGain;             // 랭크 점수 획득량
    private Integer playTime;            // 게임 시간 (초)
    @JsonProperty("startDtm")
    @JsonDeserialize(using = LocalDateTimeDeserializer.ZonedYmdHms.class)
    private LocalDateTime startDtm;       // 게임 시작 시각

    private Integer damageToPlayer;      // 총 가한 데미지
    private Integer damageFromPlayer;    // 총 받은 데미지

    private Integer[] totalVFCredits;           // 분당 획득 크레딧
    private Integer totalGainVFCredit;   // 총 획득 크레딧
    private Integer killItemBountyGainVFCredit;

    private Integer[] usedVFCredits;            // 분당 사용 크레딧
    private Integer sumUsedVFCredits;
    private Integer crUseForceCore;
    private Integer crUseTreeOfLife;
    private Integer crUseMeteorite;
    private Integer crUseMythril;
    private Integer crUseVFBloodSample;
    private Integer crUseActivationModule;

    private Integer[] itemTransferredConsole;   // 보안콘솔로 전송한 아이템
    private Integer[] itemTransferredDrone;     // 드론으로 전송한 아이템

    private Integer useSecurityConsole;         // 보안콘솔 사용 횟수

    private Integer addSurveillanceCamera;      // 설치한 감시카메라 수
    private Integer addTelephotoCamera;         // 설치한 망원카메라 수
    private Integer removeSurveillanceCamera;   // 파괴한 감시카메라 수
    private Integer removeTelephotoCamera;      // 파괴한 망원카메라 수

    private Integer useReconDrone;              // 정찰 드론 사용 횟수
    private Integer useEmpDrone;                // EMP 드론 사용 횟수

    private Integer teamElimination;            // 제거
    private Integer teamDown;                   // 빈사
    private Integer teamRepeatDown;
    private Integer teamDownCanNotEliminate;    // 사출방지(1일차) 빈사
    private Integer teamRepeatDownCanNotEliminate;
    private Integer teamDownCanEliminate;       // 사출방지(1일차) 이후 빈사
    private Integer teamRepeatDownCanEliminate;
    private Integer[] totalTKPerMin;            // 분당 킬

    private Integer enterDimensionRift;         // 균열 입장 수
    private Integer winFromDimensionRift;       // 균열 승리 수

    private Integer traitFirstCore;             // 핵심 특성
    private Integer[] traitFirstSub;            // 보조 특성 1
    private Integer[] traitSecondSub;           // 보조 특성 2

    private Integer tacticalSkillGroup;         // 전술 스킬

    private Map<Integer, Integer> equipment;    // 장착 장비 (슬롯 번호 → 아이템 코드)

    /**
     * {@code matchingMode}: 정수 코드 → {@link MatchingMode} 변환
     */
    @JsonProperty("matchingMode")
    public void setMatchingModeByCode(Integer code) {
        matchingMode = MatchingMode.from(code);
    }

    /**
     * {@code serverName}: 문자열 → {@link ServerEnum} 변환
     */
    @JsonProperty("serverName")
    public void setServerTypeByName(String serverName) {
        serverEnum = ServerEnum.fromName(serverName);
    }

    public void parsingAfter() {
        this.teamDown += teamRepeatDown;
        this.teamDownCanEliminate += teamRepeatDownCanEliminate;
        this.teamDownCanNotEliminate += teamRepeatDownCanNotEliminate;
    }

    public void arrayCutting() {
        totalVFCredits = Arrays.copyOf(totalVFCredits, arraySize(totalVFCredits));
        usedVFCredits = Arrays.copyOf(usedVFCredits, arraySize(usedVFCredits));
        totalTKPerMin = Arrays.copyOf(totalTKPerMin, arraySize(totalTKPerMin));
    }

    private int arraySize(Integer[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            Integer value = array[i];
            if (value != null && value != 0) return i + 1;
        }

        return 0;
    }

    public LocalDateTime getEndDtm() {
        return startDtm.plusSeconds(playTime);
    }

    public int getDeployCamera() {
        return addSurveillanceCamera + addTelephotoCamera;
    }

    public int getRemoveCamera() {
        return removeSurveillanceCamera + removeTelephotoCamera;
    }

    /**
     * 슬롯 번호를 인덱스로 하는 장비 목록.
     *
     * <p>{@code equipment}는 순서를 보장하지 않는 맵이므로, 슬롯 번호(0 ~ {@value #EQUIPMENT_SLOT_SIZE} - 1)를
     * 인덱스로 하는 고정 크기 리스트로 변환한다. 착용하지 않은 슬롯은 {@code null}이다.</p>
     */
    public List<Integer> getEquipments() {
        List<Integer> equipments = new ArrayList<>(EQUIPMENT_SLOT_SIZE);

        for (int slot = 0; slot < EQUIPMENT_SLOT_SIZE; slot++) {
            equipments.add(equipment == null ? null : equipment.get(slot));
        }

        return equipments;
    }

    public int getSumUsedEpicMaterialCredits() {
        return crUseActivationModule
                + crUseMeteorite
                + crUseMythril
                + crUseForceCore
                + crUseTreeOfLife
                + crUseVFBloodSample;
    }
}