package analysis;

import model.MatchContext;
import model.MatchPerformance;

public class BreakDetector {

    private static final long BREAK_THRESHOLD_HOURS = 72;

    public static MatchAnalysisContext detect(MatchPerformance current, MatchPerformance olderMatch) {
        if (olderMatch == null) return MatchAnalysisContext.normal();
        long gapMs = current.getGameStartTimestamp() - olderMatch.getGameStartTimestamp();
        long hours = gapMs / (1000L * 60 * 60);
        if (hours >= BREAK_THRESHOLD_HOURS) {
            return new MatchAnalysisContext(MatchContext.RETURNING_AFTER_BREAK, hours);
        }
        return MatchAnalysisContext.normal();
    }
}
