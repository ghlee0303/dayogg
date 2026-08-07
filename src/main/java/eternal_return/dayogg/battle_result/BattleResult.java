package eternal_return.dayogg.battle_result;

import eternal_return.dayogg.common.enums.MatchingMode;
import eternal_return.dayogg.common.enums.ServerEnum;
import eternal_return.dayogg.common.utils.ListUtils;
import eternal_return.dayogg.common.utils.converter.IntegerListConverter;
import eternal_return.dayogg.meta.meta.item.ItemInfo;
import eternal_return.dayogg.meta.meta.item.ItemMeta;
import eternal_return.dayogg.tier.enums.TierEnum;
import eternal_return.dayogg.player.Player;
import eternal_return.dayogg.statistics.Statistics;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * 게임의 전투 결과 엔티티.
 *
 * <p>
 * 외부 API({@code v1/user/games/uid/{playerId}})에서 수신한 데이터를 저장한다. <br>
 * 집계 통계는 {@link Statistics}에서 관리한다.
 * </p>
 *
 * <ul> ManyToOne
 *   <li>{@code User}</li>
 * </ul>
 */
@Entity
@Table(name = "battle_result", indexes = {
        @Index(name = "idx_br_player_season_tier_mmr",
                columnList = "player_id, season_id, tier_enum, mmr_before"),
        @Index(name = "idx_br_player_season_mmr",
                columnList = "player_id, season_id, mmr_before")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BattleResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer gameId;
    private Integer seasonId;

    @Enumerated(EnumType.STRING)
    private MatchingMode matchingMode;      // 매칭 모드
    @Enumerated(EnumType.STRING)
    private ServerEnum serverEnum;          // 서버
    @Enumerated(EnumType.STRING)
    private TierEnum tierEnum;

    private Integer characterNum;           // 캐릭터
    private Integer skinCode;              // 스킨
    private Integer characterLevel;        // 레벨
    private Integer bestWeapon;            // 주 사용 무기
    private Integer bestWeaponLevel;       // 무기 숙련도

    private Integer gameRank;              // 최종 순위
    private Integer teamNumber;            // 팀 번호
    private Integer teamKill;              // TK
    private Integer monsterKill;           // 야동 킬
    private Integer viewContribution;      // 시야 점수
    private Integer preMade;              // 사전 큐 팀원 수
    private boolean isLateGame;             // 후반게임 생존 여부: 5일차 까지

    private Integer mmrBefore;             // 게임 전 랭크 점수
    private Integer mmrGain;              // 랭크 점수 획득량
    private Integer playTime;             // 게임 시간 (초)
    private LocalDateTime startDtm;       // 게임 시작 시각

    private Integer damageToPlayer;       // 총 가한 데미지
    private Integer damageFromPlayer;     // 총 받은 데미지

    @Convert(converter = IntegerListConverter.class)
    private List<Integer> gainCredits;              // 분당 획득 크레딧
    private Integer sumGainCredits;                 // 총 획득 크레딧
    private Integer bountyGainCredits;              // 현상금 획득 크레딧
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> usedCredits;              // 분당 사용 크레딧
    private Integer sumUsedCredits;                 // 총 사용 크레딧
    private Integer sumUsedEpicMaterialCredits;     // 영웅 등급 재료 구입에 사용한 크레딧
    private Integer buyViewItemCredits;             // 카메라, 드론, EMP 드론 구입한 크레딧
    private Integer targetTimeCredits;              // 지정한 시간까지 모은 크레딧 (시즌 11기준 2낮 1분 -> 5분 10초 전 까지)

    private Integer useSecurityConsole;     // 보안콘솔 사용 횟수
    private Integer deployCamera;           // 설치한 카메라 수
    private Integer removeCamera;           // 제거한 카메라 수
    private Integer buyCamera;              // 구입한 카메라 수
    private Integer useReconDrone;          // 사용한 정찰 드론 수
    private Integer buyReconDrone;          // 구입한 정찰 드론 수
    private Integer useEmpDrone;            // 사용한 EMP 드론 수
    private Integer buyEmpDrone;            // 구입한 EMP 드론 수

    private Integer teamElimination;            // 제거
    private Integer teamDown;                   // 빈사
    private Integer teamDownCanNotEliminate;    // 사출방지(1일차) 빈사
    private Integer teamDownCanEliminate;       // 사출방지(1일차) 이후 빈사
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> totalTKPerMin;        // 분당 킬

    private Integer enterDimensionRift;         // 균열 입장 수
    private Integer winDimensionRift;       // 균열 승리 수

    private Integer traitFirstCore;             // 핵심 특성
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> traitFirstSub;            // 보조 특성 1
    @Convert(converter = IntegerListConverter.class)
    private List<Integer> traitSecondSub;           // 보조 특성 2

    private Integer tacticalSkill;         // 전술 스킬

    @Convert(converter = IntegerListConverter.class)
    private List<Integer> equipments;      // 장착 장비: 인덱스가 슬롯 번호 (무기, 옷, 머리, 팔, 다리)

    @Column(name = "player_id")
    private Long playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;

    /**
     * 1등 여부
     */
    public boolean isWin() {
        return gameRank.equals(1);
    }

    /**
     * {@code target} 등 이내 여부
     */
    public boolean isInTop(int target) {
        return gameRank <= target;
    }

    public void setUpTargetTimeVFCredit(int targetSeconds, int regenMs) {
        final int targetIndex = targetSeconds / 60;     // 2일차 낮 1분
        final int remainCredit = getCreditBySecond(targetSeconds % 60, regenMs, 1); // 2일차 낮 1650ms 마다 1크레딧 생성

        int size = gainCredits.size();

        if (size < targetIndex) {
            targetTimeCredits = ListUtils.sum(gainCredits, size);
        } else {
            targetTimeCredits = ListUtils.sum(gainCredits, targetIndex) + remainCredit;
        }
    }

    private int getCreditBySecond(int s, int ms, int value) {
        int count = (s * 1000) / ms;    // s초 동안 value를 얻는 횟수 (ms 간격)
        return count * value;
    }

    public void setUpBuyItems(
            ItemMeta itemMeta,
            List<Integer> itemTransferredConsole, List<Integer> itemTransferredDrone
    ) {
        Function<ItemInfo.PurchaseEnum, List<Integer>> getTransferred = purchaseEnum -> switch (purchaseEnum) {
            case DRONE -> itemTransferredDrone;
            case LUMI, KIOSK -> itemTransferredConsole;
        };

        ItemInfo.Purchase camera = itemMeta.getByCode(502207).transferred(getTransferred);
        ItemInfo.Purchase reconDrone = itemMeta.getByCode(502208).transferred(getTransferred);
        ItemInfo.Purchase empDrone = itemMeta.getByCode(502308).transferred(getTransferred);

        this.buyCamera = camera.qty();
        this.buyReconDrone = reconDrone.qty();
        this.buyEmpDrone = empDrone.qty();

        this.buyViewItemCredits = camera.cost() + reconDrone.cost() + empDrone.cost();
    }

    public void setUpLateGame(int targetSeconds) {
        this.isLateGame = playTime > targetSeconds;
    }
}