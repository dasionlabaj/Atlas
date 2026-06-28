package analysis.coach;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import model.MatchPerformance;
import model.coach.CoachReflection;
import model.coach.ObservationGauge;
import model.coach.PlayerFocusProfile;
import model.coach.SessionBattery;

/**
 * Costruisce il profilo osservato da partite reali.
 * Le soglie di riferimento (quanto vale "100%") sono calibrazione v0.1: arbitrarie,
 * da raffinare. I valori, i trend e la confidenza invece sono calcolati dai dati Riot.
 *
 * Assunzione di ordinamento: matches.get(0) = partita più recente (come nel resto di Atlas).
 */
public class CoachProfileAnalyzer {

    // --- calibrazione v0.1: il valore della metrica che mappa a gauge 100% ---
    private static final double CS_PER_MIN_AT_100     = 10.0;
    private static final double VISION_PER_MIN_AT_100 = 2.0;
    private static final double DEATHS_AT_ZERO        = 10.0;  // 10 morti medie → gauge 0
    private static final double KDA_AT_100            = 5.0;

    private static final double TREND_THRESHOLD = 5.0; // punti gauge per dichiarare un trend

    private final SessionBatteryAnalyzer batteryAnalyzer = new SessionBatteryAnalyzer();
    private final CoachReflectionGenerator reflectionGenerator = new CoachReflectionGenerator();
    private final CoachHypothesisGenerator hypothesisGenerator = new CoachHypothesisGenerator();

    public PlayerFocusProfile analyze(List<MatchPerformance> matches) {
        List<MatchPerformance> safe = matches == null ? List.of() : matches;

        ObservationGauge cs = gauge("CS",
                values(safe, m -> safeDiv(m.getCs(), m.getMinutes())),
                v -> pct(v / CS_PER_MIN_AT_100));

        ObservationGauge vision = gauge("Vision",
                values(safe, m -> safeDiv(m.getVisionScore(), m.getMinutes())),
                v -> pct(v / VISION_PER_MIN_AT_100));

        ObservationGauge deaths = gauge("Deaths",
                values(safe, m -> (double) m.getDeaths()),
                v -> pct(1.0 - v / DEATHS_AT_ZERO));

        ObservationGauge kda = gauge("KDA",
                values(safe, m -> (m.getKills() + m.getAssists()) / Math.max(1.0, m.getDeaths())),
                v -> pct(v / KDA_AT_100));

        SessionBattery battery = batteryAnalyzer.analyze(safe);
        List<model.coach.CoachHypothesis> hypotheses =
                hypothesisGenerator.generate(cs, vision, deaths, kda, battery, safe.size());
        CoachReflection reflection = reflectionGenerator.generate(cs, vision, deaths, kda, battery);

        return new PlayerFocusProfile(cs, vision, deaths, kda, battery, hypotheses, reflection);
    }

    /** Estrae il valore grezzo di una metrica per ogni partita. */
    private List<Double> values(List<MatchPerformance> matches,
                                java.util.function.ToDoubleFunction<MatchPerformance> metric) {
        List<Double> out = new ArrayList<>(matches.size());
        for (MatchPerformance m : matches) out.add(metric.applyAsDouble(m));
        return out;
    }

    /**
     * Costruisce un gauge: valore = media trasformata in scala 0-100;
     * trend = metà recente vs metà vecchia; confidenza = consistenza × ampiezza campione.
     */
    private ObservationGauge gauge(String name, List<Double> raw, DoubleUnaryOperator toGauge) {
        if (raw.isEmpty()) {
            return new ObservationGauge(name, 0.0, 0.0, "incerto");
        }

        double value = toGauge.applyAsDouble(mean(raw));

        // Trend: media prima metà (più recente) vs seconda metà (più vecchia)
        int half = raw.size() / 2;
        String trend;
        if (half == 0) {
            trend = "incerto";
        } else {
            double recent = toGauge.applyAsDouble(mean(raw.subList(0, half)));
            double older  = toGauge.applyAsDouble(mean(raw.subList(raw.size() - half, raw.size())));
            double delta  = recent - older;
            if (delta > TREND_THRESHOLD)       trend = "in miglioramento";
            else if (delta < -TREND_THRESHOLD) trend = "in calo";
            else                               trend = "stabile";
        }

        double confidence = confidence(raw);
        return new ObservationGauge(name, value, confidence, trend);
    }

    /** Confidenza: alta se la metrica è consistente e il campione è ampio. */
    private double confidence(List<Double> raw) {
        double mean = mean(raw);
        double cv = mean > 0.0 ? stdDev(raw, mean) / mean : 1.0; // coefficiente di variazione
        double consistency = Math.max(0.0, 1.0 - Math.min(1.0, cv));
        double sampleFactor = Math.min(1.0, raw.size() / 10.0);
        return clamp(100.0 * consistency * sampleFactor);
    }

    private double mean(List<Double> xs) {
        double s = 0.0;
        for (double x : xs) s += x;
        return s / xs.size();
    }

    private double stdDev(List<Double> xs, double mean) {
        if (xs.size() < 2) return 0.0;
        double s = 0.0;
        for (double x : xs) s += (x - mean) * (x - mean);
        return Math.sqrt(s / xs.size());
    }

    private double safeDiv(double a, double b) { return b > 0.0 ? a / b : 0.0; }
    private double pct(double ratio)           { return clamp(ratio * 100.0); }
    private double clamp(double v)             { return Math.max(0.0, Math.min(100.0, v)); }
}
