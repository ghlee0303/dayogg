package eternal_return.dayogg.player.client;

import eternal_return.dayogg.common.enums.ServerEnum;

public record PlayerSeasonApiResponse(
        Integer mmr,
        Integer rank,
        Integer serverRank,
        Integer serverCode
) {
    public ServerEnum serverEnum() {
        return ServerEnum.fromCode(serverCode);
    }
}
