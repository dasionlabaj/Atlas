package analysis.rank;

import model.rank.Rank;
import model.rank.RankDivision;
import model.rank.RankGap;

public class RankGapAnalyzer {

    public RankGap compute(Rank predicted, Rank actual) {
        int gap = toOrdinal(predicted) - toOrdinal(actual);
        return new RankGap(predicted, actual, gap);
    }

    // Tier IV=0 .. I=3 within each tier; enum ordinals match LoL ordering (IV lowest, I highest)
    private int toOrdinal(Rank rank) {
        if (rank.division() == RankDivision.NONE) {
            return rank.tier().ordinal() * 4;
        }
        return rank.tier().ordinal() * 4 + rank.division().ordinal();
    }
}
