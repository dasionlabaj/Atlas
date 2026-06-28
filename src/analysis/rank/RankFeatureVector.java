package analysis.rank;

public record RankFeatureVector(
    double winRate,
    double kdaAverage,
    double visionScoreAverage,
    double damagePerMinuteAverage,
    double csPerMinuteAverage,
    int sampleSize
) {}
