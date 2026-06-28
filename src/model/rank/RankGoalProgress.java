package model.rank;

public record RankGoalProgress(
    Rank currentRank,
    Rank targetRank,
    int remainingDivisions,
    double completionPercentage
) {}
