package analysis.rank;

import model.rank.RankPrediction;

public record RankPredictionResult(
    RankPrediction prediction,
    RankFeatureVector inputFeatures,
    double score
) {}
