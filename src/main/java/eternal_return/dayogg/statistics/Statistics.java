package eternal_return.dayogg.statistics;

import eternal_return.dayogg.battle_result.BattleResult;
import eternal_return.dayogg.common.enums.RangeSideEnum;
import eternal_return.dayogg.common.enums.ServerEnum;
import eternal_return.dayogg.player.Player;
import eternal_return.dayogg.statistics.extend.AverageStatistics;
import eternal_return.dayogg.statistics.range.RangeSide;
import eternal_return.dayogg.statistics.range.StatisticsRange;
import eternal_return.dayogg.tier.enums.TierEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Statistics extends AverageStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ServerEnum serverEnum;

    @Enumerated(EnumType.STRING)
    private TierEnum tierEnum;

    @Column(name = "player_id")
    private Long playerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;

    @OneToMany(mappedBy = "statistics", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "sideEnum")
    private Map<RangeSideEnum, StatisticsRange> range = new EnumMap<>(RangeSideEnum.class);

    private Integer seasonId;
    private Integer characterNum;
    private Integer weaponNum;

    public Statistics(BattleResult battleResult, TierEnum tierEnum) {
        this.serverEnum = battleResult.getServerEnum();
        this.tierEnum = tierEnum;
        this.playerId = battleResult.getPlayerId();

        this.seasonId = battleResult.getSeasonId();
        this.characterNum = battleResult.getCharacterNum();
        this.weaponNum = battleResult.getBestWeapon();
        this.range = StatisticsRange.mapCreator(this);
    }

    @Override
    public void merge(BattleResult battleResult) {
        super.merge(battleResult);
        RangeSide.merge(
                this.range,
                battleResult.getMmrBefore(),
                battleResult.getStartDtm()
        );
    }
}