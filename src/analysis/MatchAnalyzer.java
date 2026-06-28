package analysis;

import java.util.ArrayList;
import java.util.List;

import model.MatchPerformance;
import model.StatSignal;
import policy.Metric;
import policy.ThresholdEvaluation;
import policy.ThresholdProvider;

/**
 * Traduce una partita in segnali. Non spiega più la formula: chiede alla soglia
 * cosa significa il numero. Il reason di ogni StatSignal è un'osservazione in
 * lingua umana, non una prescrizione — il consiglio è un altro livello (Coach).
 */
public class MatchAnalyzer {

    private ThresholdProvider thresholds;

    public MatchAnalyzer(ThresholdProvider thresholds) {
        this.thresholds = thresholds;
    }

    public List<StatSignal> analyze(MatchPerformance performance) {
        List<StatSignal> signals = new ArrayList<>();
        signals.add(signal("KDA",        Metric.KDA,               kda(performance)));
        signals.add(signal("CS/min",     Metric.CS_PER_MINUTE,     csPerMinute(performance)));
        signals.add(signal("Vision/min", Metric.VISION_PER_MINUTE, visionPerMinute(performance)));
        return signals;
    }

    private StatSignal signal(String name, Metric metric, double value) {
        ThresholdEvaluation evaluation = thresholds.evaluate(metric, value);
        return new StatSignal(name, evaluation.getColor(), value,
                evaluation.getGreenThreshold(), evaluation.getYellowThreshold(),
                evaluation.getReason());
    }

    private double kda(MatchPerformance performance) {
        return (performance.getKills() + performance.getAssists())
                / Math.max(1.0, performance.getDeaths());
    }

    private double csPerMinute(MatchPerformance performance) {
        return performance.getCs() / performance.getMinutes();
    }

    private double visionPerMinute(MatchPerformance performance) {
        return performance.getVisionScore() / performance.getMinutes();
    }
}
