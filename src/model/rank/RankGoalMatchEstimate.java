package model.rank;

/**
 * Distanza dall'obiettivo tradotta in tempo stimato (pseudo-partite positive).
 * Oggetto distinto da RankGoalProgress: "distanza ↔ tempo", non "realtà ↔ obiettivo".
 * gamesPerDivision è calibrazione v0.1; diventerà 100 / averageNetLpGain con LP reali.
 */
public record RankGoalMatchEstimate(
    int remainingDivisions,
    int gamesPerDivision,
    int pseudoGamesRemaining
) {}
