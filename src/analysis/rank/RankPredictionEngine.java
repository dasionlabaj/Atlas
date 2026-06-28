package analysis.rank;

import model.rank.Rank;
import model.rank.RankDivision;
import model.rank.RankPrediction;
import model.rank.RankTier;

public class RankPredictionEngine {

    // v0.1 — pesi euristici, da calibrare su dati reali
    private static final double WEIGHT_WIN_RATE       = 40.0;
    private static final double WEIGHT_KDA            =  5.0;
    private static final double WEIGHT_CS_PER_MINUTE  =  3.0;
    private static final double WEIGHT_VISION         =  0.3;
    private static final double WEIGHT_DAMAGE_PER_MIN =  0.005;

    // soglie di tier: score 0 → 100+
    private static final double[] TIER_THRESHOLDS = { 15.0, 30.0, 45.0, 60.0, 75.0, 90.0, 100.0 };
    private static final RankTier[] TIER_MAP = {
        RankTier.IRON, RankTier.BRONZE, RankTier.SILVER,
        RankTier.GOLD, RankTier.PLATINUM, RankTier.EMERALD,
        RankTier.DIAMOND, RankTier.MASTER
    };

    public RankPredictionResult predict(RankFeatureVector features) {
        double score = calculateScore(features);
        Rank rank = scoreToRank(score);
        double confidence = calculateConfidence(score, features.sampleSize());
        return new RankPredictionResult(new RankPrediction(rank, confidence), features, score);
    }

    private double calculateScore(RankFeatureVector f) {
        return f.winRate()                 * WEIGHT_WIN_RATE
             + f.kdaAverage()             * WEIGHT_KDA
             + f.csPerMinuteAverage()     * WEIGHT_CS_PER_MINUTE
             + f.visionScoreAverage()     * WEIGHT_VISION
             + f.damagePerMinuteAverage() * WEIGHT_DAMAGE_PER_MIN;
    }

    private Rank scoreToRank(double score) {
        int i = findTierIndex(score);
        RankTier tier = TIER_MAP[i];

        if (tier == RankTier.MASTER || tier == RankTier.GRANDMASTER || tier == RankTier.CHALLENGER) {
            return new Rank(tier, RankDivision.NONE, 0);
        }

        double floor   = i == 0 ? 0.0 : TIER_THRESHOLDS[i - 1];
        double ceiling = TIER_THRESHOLDS[i];
        double pos = (score - floor) / (ceiling - floor); // 0.0 → 1.0 dentro il tier

        RankDivision division;
        if      (pos < 0.25) division = RankDivision.IV;
        else if (pos < 0.50) division = RankDivision.III;
        else if (pos < 0.75) division = RankDivision.II;
        else                 division = RankDivision.I;

        return new Rank(tier, division, 0);
    }

    private double calculateConfidence(double score, int sampleSize) {
        int i = findTierIndex(score);
        double floor   = i == 0 ? 0.0 : TIER_THRESHOLDS[i - 1];
        double ceiling = i < TIER_THRESHOLDS.length ? TIER_THRESHOLDS[i] : 130.0;
        double pos = (score - floor) / (ceiling - floor);

        // massima al centro del tier, minima ai bordi
        double borderDistance = Math.min(pos, 1.0 - pos) * 2.0;
        double base = 0.5 + borderDistance * 0.4; // [0.5, 0.9]

        // piena confidenza a 20+ partite
        double sampleFactor = Math.min(sampleSize / 20.0, 1.0);
        return base * (0.6 + 0.4 * sampleFactor); // [0.3, 0.9]
    }

    private int findTierIndex(double score) {
        for (int i = 0; i < TIER_THRESHOLDS.length; i++) {
            if (score < TIER_THRESHOLDS[i]) return i;
        }
        return TIER_THRESHOLDS.length; // MASTER
    }
}
