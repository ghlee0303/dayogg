package eternal_return.dayogg.player.dto;

import eternal_return.dayogg.player.Player;

import java.time.LocalDateTime;

public record PlayerDto(
        Long id, String searchId, Integer level, String name, LocalDateTime lastSearchTime
) {
    public static PlayerDto from(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getSearchId(),
                player.getLevel(),
                player.getName(),
                player.getLastSearchTime()
        );
    }
}
