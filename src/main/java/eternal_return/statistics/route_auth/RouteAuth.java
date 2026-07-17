package eternal_return.statistics.route_auth;

import eternal_return.statistics.player.Player;
import eternal_return.statistics.route_auth.client.RouteApiResponse;
import eternal_return.statistics.route_auth.enums.RouteAuthEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@NoArgsConstructor
@Getter
public class RouteAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String routeId;
    private String title;
    private String playerName;

    @Enumerated(EnumType.STRING)
    private RouteAuthEnum status;

    @Column(name = "player_id")
    private Long playerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public RouteAuth(String routeId, Long playerId, String title, String playerName) {
        this.routeId = routeId;
        this.playerId = playerId;
        this.title = title;
        this.playerName = playerName;
        LocalDateTime now = LocalDateTime.now();
        this.createAt = now;
        this.updateAt = now;
    }

    public void touch() {
        this.updateAt = LocalDateTime.now();
    }

    public boolean authentication(RouteApiResponse apiResponse, int timeoutMinutes) {
        if (title.equals(apiResponse.getTitle())
                && playerName.equals(apiResponse.getUserNickName())) {
            this.status = RouteAuthEnum.OK;
            return true;
        }

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
        if (timeoutThreshold.isAfter(createAt)) {
            this.status = RouteAuthEnum.TIMEOUT;
            return false;
        }

        return false;
    }
}
