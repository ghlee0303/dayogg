package eternal_return.dayogg.battle_result.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import eternal_return.dayogg.common.utils.converter.IntegerListConverter;
import eternal_return.dayogg.tier.enums.TierEnum;
import jakarta.persistence.Convert;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleResponse {
    private Long playerId;
    private Integer gameId;
    private Integer seasonId;
    private TierEnum tierEnum;

    private Integer characterNum;           // 캐릭터
    private Integer skinCode;              // 스킨
    private Integer characterLevel;        // 레벨
    private Integer bestWeapon;            // 주 사용 무기
    private Integer bestWeaponLevel;       // 무기 숙련도

    private Integer gameRank;              // 최종 순위
    private Integer teamKill;              // TK
    private Integer viewContribution;      // 시야 점수
    @JsonProperty("isLateGame")
    private boolean isLateGame;             // 후반게임 생존 여부: 5일차 까지

    private Integer mmrBefore;             // 게임 전 랭크 점수
    private Integer mmrGain;              // 랭크 점수 획득량
    private Integer playTime;             // 게임 시간 (초)
    private LocalDateTime startDtm;       // 게임 시작 시각

    private Integer damageToPlayer;       // 총 가한 데미지

    private Integer sumGainCredits;        // 총 획득 크레딧
    private Integer targetTimeCredits;     // 지정한 시간까지 모은 크레딧 (시즌 11기준 2낮 1분 -> 5분 10초 전 까지)

    private Integer useSecurityConsole;     // 보안콘솔 사용 횟수

    private Integer teamElimination;            // 제거
    private Integer teamDown;                   // 빈사
    private Integer teamDownCanNotEliminate;    // 사출방지(1일차) 빈사
    private Integer teamDownCanEliminate;       // 사출방지(1일차) 이후 빈사

    private Integer traitFirstCore;             // 핵심 특성
    private List<Integer> traitFirstSub;            // 보조 특성 1
    private List<Integer> traitSecondSub;           // 보조 특성 2

    private Integer tacticalSkill;         // 전술 스킬

    private List<Integer> equipments;      // 장착 장비: 인덱스가 슬롯 번호 (무기, 옷, 머리, 팔, 다리)
}
