package eternal_return.dayogg.meta.meta.top_tier_cut;

public record TopTierCutInfo(
        Integer demiGodRank,
        Integer demiGodCut,
        Integer eternityRank,
        Integer eternityCut
) {
    public boolean isSame(int demiGodCut, int eternityCut) {
        return this.demiGodCut == demiGodCut && this.eternityCut == eternityCut;
    }

    public boolean isDemiGod(int rank) {
        return this.demiGodRank == rank;
    }

    public boolean isEternity(int rank) {
        return this.eternityRank == rank;
    }

}
