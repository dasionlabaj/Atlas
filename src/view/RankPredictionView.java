package view;

import analysis.rank.RankPredictionResult;
import model.rank.Rank;

public class RankPredictionView {

    public void show(RankPredictionResult result) {
        Rank rank = result.prediction().predictedRank();

        System.out.println("=== Rank Prediction ===");
        System.out.printf("Win rate:   %.1f%%%n",     result.inputFeatures().winRate() * 100);
        System.out.printf("KDA avg:    %.2f%n",       result.inputFeatures().kdaAverage());
        System.out.printf("CS/min avg: %.1f%n",       result.inputFeatures().csPerMinuteAverage());
        System.out.printf("Vision avg: %.1f%n",       result.inputFeatures().visionScoreAverage());
        System.out.printf("DPM avg:    %.0f%n",       result.inputFeatures().damagePerMinuteAverage());
        System.out.printf("Sample:     %d matches%n", result.inputFeatures().sampleSize());
        System.out.println("-----------------------");
        System.out.printf("Predicted:  %s %s%n",      rank.tier(), rank.division());
        System.out.printf("Confidence: %.0f%%%n",     result.prediction().confidence() * 100);
        System.out.println("=======================");
    }
}
