package eternal_return.dayogg.player;

import eternal_return.dayogg.player.player_season.PlayerSeason;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "player",
        indexes = {
                @Index(name = "idx_player_name", columnList = "name")
        }
)
@NoArgsConstructor
@Getter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String searchId;
    private String name;
    private Integer level;

    private LocalDateTime lastSearchTime;

    private Boolean isDeleted;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private List<PlayerSeason> playerSeasons = new ArrayList<>();

    public Player(String name, String searchId) {
        this.searchId = searchId;
        this.name = name;
        isDeleted = false;
    }

    public void deleted() {
        isDeleted = true;
    }

    public void updateLastSearchTime() {
        lastSearchTime = LocalDateTime.now();
    }

    public void updateLevel(Integer level) {
        if (level == null) return;

        if (this.level == null) {
            this.level = level;
            return;
        }

        this.level = Math.max(this.level, level);
    }

    public boolean isNew() {
        return level == null || lastSearchTime == null;
    }
}
