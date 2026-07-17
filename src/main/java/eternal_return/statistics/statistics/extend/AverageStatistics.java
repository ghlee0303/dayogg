package eternal_return.statistics.statistics.extend;

import eternal_return.statistics.battle_result.BattleResult;
import eternal_return.statistics.common.utils.MathUtils;
import eternal_return.statistics.statistics.Statistics;
import eternal_return.statistics.statistics.dto.response.StatisticsResponse;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.BiFunction;

/**
 * {@link Statistics}와 {@link StatisticsResponse}가
 * 공유하는 누적·평균 필드 및 병합 로직.
 *
 * <p>{@code @MappedSuperclass}로 선언되어 {@link Statistics} 엔티티의 컬럼으로 포함되며,
 * DTO에서는 일반 Java 상속으로 재사용된다.
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public class AverageStatistics {
    // 누적
    protected int totalGames;       // 총 게임 수
    protected int wins;         // 승리 수
    protected int top2;         // TOP 2 수
    protected int top3;         // TOP 3 수
    protected int sumMmrGain;      // 누적 MMR 획득량

    protected int lateGameCount;                // 후반게임 생존 횟수: 5일차 까지
    protected int preMadeGameCount;             // 사전큐 게임 수
    protected int enterDimensionRiftCount;      // 균열 입장 수
    protected int winDimensionRiftCount;        // 균열 승리 수

    // 평균
    protected float gameRank;           // 평균 순위
    protected float teamKill;           // 평균 팀킬
    protected float monsterKill;        // 평균 몬스터 킬
    protected float viewContribution;   // 평균 시야 점수
    protected float playTime;           // 평균 게임 시간 (초)

    protected float damageToPlayer;     // 평균 가한 데미지
    protected float damageFromPlayer;   // 평균 받은 데미지

    protected float gainCredits;                // 획득 크레딧
    protected float bountyGainCredits;          // 현상금 크레딧
    protected float usedCredits;                // 사용 크레딧
    protected float usedEpicMaterialCredits;
    protected float buyViewItemCredits;         //  카메라, 드론, EMP 드론 구입한 크레딧
    protected float targetTimeCredits;          // 지정한 시간까지 모은 크레딧

    protected float useSecurityConsole;     // 보안콘솔 사용 횟수
    protected float deployCamera;           // 설치한 카메라 수
    protected float removeCamera;           // 제거한 카메라 수
    protected float buyCamera;              // 구입한 카메라 수
    protected float buyReconDrone;          // 구입한 드론 사용 횟수
    protected float useReconDrone;          // 정찰 드론 사용 횟수
    protected float buyEmpDrone;            // 구입한 드론 사용 횟수
    protected float useEmpDrone;            // EMP 드론 사용 횟수

    protected float teamElimination;            // 제거
    protected float teamDown;                   // 빈사
    protected float teamDownCanNotEliminate;    // 사출방지(1일차) 빈사
    protected float teamDownCanEliminate;       // 사출방지(1일차) 이후 빈사

    public AverageStatistics(BattleResult battleResult) {
        this.totalGames = 1;
        this.sumMmrGain = battleResult.getMmrGain();
        this.wins = battleResult.isWin() ? 1 : 0;
        this.top2 = battleResult.isInTop(2) ? 1 : 0;
        this.top3 = battleResult.isInTop(3) ? 1 : 0;
        this.lateGameCount = battleResult.isLateGame() ? 1 : 0;
        this.preMadeGameCount = battleResult.getPreMade() > 1 ? 1 : 0;

        this.enterDimensionRiftCount = battleResult.getEnterDimensionRift();
        this.winDimensionRiftCount = battleResult.getWinDimensionRift();

        //----

        this.gameRank = battleResult.getGameRank();
        this.teamKill = battleResult.getTeamKill();
        this.monsterKill = battleResult.getMonsterKill();
        this.viewContribution = battleResult.getViewContribution();
        this.playTime = battleResult.getPlayTime();

        this.damageToPlayer = battleResult.getDamageToPlayer();
        this.damageFromPlayer = battleResult.getDamageFromPlayer();

        this.gainCredits = battleResult.getSumGainCredits();
        this.bountyGainCredits = battleResult.getBountyGainCredits();
        this.usedCredits = battleResult.getSumUsedCredits();
        this.usedEpicMaterialCredits = battleResult.getSumUsedEpicMaterialCredits();
        this.buyViewItemCredits = battleResult.getBuyViewItemCredits();
        this.targetTimeCredits = battleResult.getTargetTimeCredits();

        this.useSecurityConsole = battleResult.getUseSecurityConsole();
        this.deployCamera = battleResult.getDeployCamera();
        this.removeCamera = battleResult.getRemoveCamera();
        this.buyCamera = battleResult.getBuyCamera();
        this.useReconDrone = battleResult.getUseReconDrone();
        this.buyReconDrone = battleResult.getBuyReconDrone();
        this.useEmpDrone = battleResult.getUseEmpDrone();
        this.buyEmpDrone = battleResult.getBuyEmpDrone();

        this.teamElimination = battleResult.getTeamElimination();
        this.teamDown = battleResult.getTeamDown();
        this.teamDownCanNotEliminate = battleResult.getTeamDownCanNotEliminate();
        this.teamDownCanEliminate = battleResult.getTeamDownCanEliminate();
    }

    /**
     * 다른 집계 결과를 현재 인스턴스에 병합한다.
     * <p>
     * 누적 필드는 합산하고, 평균 필드는 {@code totalGames}를 가중치로 한 가중 평균으로 갱신한다.
     *
     * @param other 병합할 집계 데이터
     */
    public void merge(AverageStatistics other) {
        int prevGames = this.totalGames;

        // 누적 합산
        this.totalGames += other.totalGames;
        this.wins += other.wins;
        this.top2 += other.top2;
        this.top3 += other.top3;
        this.sumMmrGain += other.sumMmrGain;
        this.lateGameCount += other.lateGameCount;
        this.preMadeGameCount += other.preMadeGameCount;

        this.enterDimensionRiftCount += other.enterDimensionRiftCount;
        this.winDimensionRiftCount += other.winDimensionRiftCount;

        if (this.totalGames == 0) return;

        // totalGames 가중 평균
        BiFunction<Float, Float, Float> wa =
                (a, b) -> MathUtils.weightedAverage(a, b, prevGames, other.totalGames);

        this.gameRank = wa.apply(this.gameRank, other.gameRank);
        this.teamKill = wa.apply(this.teamKill, other.teamKill);
        this.monsterKill = wa.apply(this.monsterKill, other.monsterKill);
        this.viewContribution = wa.apply(this.viewContribution, other.viewContribution);
        this.playTime = wa.apply(this.playTime, other.playTime);
        this.damageToPlayer = wa.apply(this.damageToPlayer, other.damageToPlayer);

        this.gainCredits = wa.apply(this.gainCredits, other.gainCredits);
        this.usedCredits = wa.apply(this.usedCredits, other.usedCredits);
        this.usedEpicMaterialCredits = wa.apply(this.usedEpicMaterialCredits, other.usedEpicMaterialCredits);
        this.buyViewItemCredits = wa.apply(this.buyViewItemCredits, other.buyViewItemCredits);
        this.targetTimeCredits = wa.apply(this.targetTimeCredits, other.targetTimeCredits);

        this.useSecurityConsole = wa.apply(this.useSecurityConsole, other.useSecurityConsole);
        this.deployCamera = wa.apply(this.deployCamera, other.deployCamera);
        this.removeCamera = wa.apply(this.removeCamera, other.removeCamera);
        this.buyCamera = wa.apply(this.buyCamera, other.buyCamera);
        this.useReconDrone = wa.apply(this.useReconDrone, other.useReconDrone);
        this.buyReconDrone = wa.apply(this.buyReconDrone, other.buyReconDrone);
        this.useEmpDrone = wa.apply(this.useEmpDrone, other.useEmpDrone);
        this.buyEmpDrone = wa.apply(this.buyEmpDrone, other.buyEmpDrone);

        this.teamElimination = wa.apply(this.teamElimination, other.teamElimination);
        this.teamDown = wa.apply(this.teamDown, other.teamDown);
        this.teamDownCanNotEliminate = wa.apply(this.teamDownCanNotEliminate, other.teamDownCanNotEliminate);
        this.teamDownCanEliminate = wa.apply(this.teamDownCanEliminate, other.teamDownCanEliminate);

    }

    public void merge(BattleResult battleResult) {
        merge(new AverageStatistics(battleResult));
    }
}
