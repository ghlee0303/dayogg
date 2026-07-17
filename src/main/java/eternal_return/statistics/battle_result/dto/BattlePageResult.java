package eternal_return.statistics.battle_result.dto;

import java.util.List;

/**
 * 전적 범위 조회의 offset 페이지네이션 결과.
 *
 * @param games         현재 페이지의 전적 목록 (최신순)
 * @param page          현재 페이지 번호 (0-base)
 * @param size          페이지 크기
 * @param totalElements 조건에 해당하는 전체 전적 수
 * @param totalPages    전체 페이지 수
 */
public record BattlePageResult(
        List<BattleResponse> games,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static BattlePageResult of(List<BattleResponse> games, int page, int size, long totalElements) {
        int totalPages = (int) ((totalElements + size - 1) / size);
        return new BattlePageResult(games, page, size, totalElements, totalPages);
    }
}
