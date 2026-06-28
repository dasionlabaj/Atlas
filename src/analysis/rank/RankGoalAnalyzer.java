package analysis.rank;

import model.rank.Rank;
import model.rank.RankDivision;
import model.rank.RankGoalProgress;

public class RankGoalAnalyzer {

    public RankGoalProgress compute(Rank current, Rank target) {
        int currentOrd  = toOrdinal(current);
        int targetOrd   = toOrdinal(target);
        int remaining   = Math.max(0, targetOrd - currentOrd);
        double pct      = targetOrd == 0 ? 100.0 : Math.min(100.0, (double) currentOrd / targetOrd * 100.0);
        return new RankGoalProgress(current, target, remaining, pct);
    }

    private int toOrdinal(Rank rank) {
        if (rank.division() == RankDivision.NONE) {
            return rank.tier().ordinal() * 4;
        }
        return rank.tier().ordinal() * 4 + rank.division().ordinal();
    }
}
