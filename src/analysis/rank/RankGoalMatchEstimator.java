package analysis.rank;

import model.rank.RankGoalMatchEstimate;
import model.rank.RankGoalProgress;

public class RankGoalMatchEstimator {

    // calibrazione v0.1: 1 divisione ~ 100 LP, una win netta ~ +25 LP -> ~4 partite.
    // Diventerà 100 / averageNetLpGain quando Atlas leggerà gli LP reali.
    private static final int GAMES_PER_DIVISION = 4;

    public RankGoalMatchEstimate estimate(RankGoalProgress progress) {
        int games = progress.remainingDivisions() * GAMES_PER_DIVISION;
        return new RankGoalMatchEstimate(progress.remainingDivisions(), GAMES_PER_DIVISION, games);
    }
}
