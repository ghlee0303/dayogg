package eternal_return.statistics.player.client;

import eternal_return.statistics.common.enums.ServerEnum;

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
