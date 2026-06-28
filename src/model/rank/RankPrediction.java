package model.rank;

public record RankPrediction(
    Rank predictedRank,
    double confidence
) {}
