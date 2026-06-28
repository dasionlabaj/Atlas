package analysis;

import model.MatchContext;

public class MatchAnalysisContext {

    private final MatchContext matchContext;
    private final long hoursSinceLastGame;

    public MatchAnalysisContext(MatchContext matchContext, long hoursSinceLastGame) {
        this.matchContext       = matchContext;
        this.hoursSinceLastGame = hoursSinceLastGame;
    }

    public static MatchAnalysisContext normal() {
        return new MatchAnalysisContext(MatchContext.NORMAL, 0);
    }

    public MatchContext getMatchContext()    { return matchContext; }
    public long getHoursSinceLastGame()      { return hoursSinceLastGame; }

    public boolean isReturningAfterBreak() {
        return matchContext == MatchContext.RETURNING_AFTER_BREAK;
    }

    @Override
    public String toString() {
        if (matchContext == MatchContext.NORMAL) return "";
        return "[RETURNING AFTER BREAK +" + hoursSinceLastGame + "h]";
    }
}
