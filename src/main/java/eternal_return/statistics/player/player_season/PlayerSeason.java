package eternal_return.statistics.player.player_season;

import eternal_return.statistics.common.enums.ServerEnum;
import eternal_return.statistics.tier.enums.TierEnum;
import eternal_return.statistics.player.Player;
import eternal_return.statistics.player.client.PlayerSeasonApiResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor
public class PlayerSeason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer seasonId;

    @Enumerated(EnumType.STRING)
    private ServerEnum serverEnum;

    @Enumerated(EnumType.STRING)
    private TierEnum tierEnum;

    private Integer mmr;
    
    @Column(name = "p_rank")
    private Integer rank;
    private Integer serverRank;

    @Column(name = "player_id")
    private Long playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;

    public PlayerSeason(Long playerId, Integer seasonId) {
        this.playerId = playerId;
        this.seasonId = seasonId;
    }

    public void update(PlayerSeasonApiResponse apiResponse, TierEnum tierEnum) {
        this.serverEnum = apiResponse.serverEnum();
        this.tierEnum = tierEnum;
        this.mmr = apiResponse.mmr();
        this.rank = apiResponse.rank();
        this.serverRank = apiResponse.serverRank();
    }
}