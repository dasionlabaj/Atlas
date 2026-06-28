package analysis.coach;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.MatchPerformance;
import model.coach.SessionBattery;

/**
 * Stima la capacità cognitiva residua osservata.
 *
 * NON dipende dal numero di partite. Dipende dalla consistenza:
 *   - retention: quanto le partite recenti tengono il livello di inizio sessione
 *   - consistency: quanto le metriche sono stabili (poca varianza)
 *
 * Due giorni con lo stesso numero di game possono dare batterie diverse:
 * dieci partite consistenti restano alte, due partite con CS che crolla precipitano.
 * Atlas non misura la stanchezza col cronometro: la deduce dalla deriva dei dati.
 *
 * Assunzione: matches.get(0) = partita più recente.
 */
public class SessionBatteryAnalyzer {

    private static final long SESSION_GAP_MS = 3L * 60 * 60 * 1000; // 3 ore

    // calibrazione v0.1: peso di tenuta vs. peso di stabilità
    private static final double W_RETENTION   = 0.65;
    private static final double W_CONSISTENCY = 0.35;

    public SessionBattery analyze(List<MatchPerformance> matches) {
        if (matches == null || matches.isEmpty()) {
            return new SessionBattery(100, "nessuna sessione osservata");
        }

        // Raccoglie la sessione corrente (newest-first), poi la ordina cronologicamente
        List<MatchPerformance> session = new ArrayList<>();
        session.add(matches.get(0));
        for (int i = 1; i < matches.size(); i++) {
            long gap = Math.abs(matches.get(i - 1).getGameStartTimestamp()
                              - matches.get(i).getGameStartTimestamp());
            if (gap > SESSION_GAP_MS) break;
            session.add(matches.get(i));
        }

        int games = session.size();
        if (games == 1) {
            return new SessionBattery(100, "sessione appena iniziata (1 game)");
        }

        Collections.reverse(session); // primo elemento = inizio sessione

        List<Double> cs = new ArrayList<>();
        List<Double> vision = new ArrayList<>();
        List<Double> kda = new ArrayList<>();
        for (MatchPerformance m : session) {
            double min = m.getMinutes() > 0.0 ? m.getMinutes() : 1.0;
            cs.add(m.getCs() / min);
            vision.add(m.getVisionScore() / min);
            kda.add((m.getKills() + m.getAssists()) / Math.max(1.0, m.getDeaths()));
        }

        int half = games / 2;
        double retention = mean(
                retention(cs, half),
                retention(vision, half),
                retention(kda, half));
        double consistency = consistency(cs);

        int battery = clamp((int) Math.round(100.0 * (W_RETENTION * retention + W_CONSISTENCY * consistency)));
        return new SessionBattery(battery, status(battery, games));
    }

    /** Quanto la seconda metà tiene rispetto alla prima (0-1, metrica higher-better). */
    private double retention(List<Double> xs, int half) {
        double baseline = mean(sub(xs, 0, half));
        double recent   = mean(sub(xs, xs.size() - half, xs.size()));
        if (baseline <= 0.0) return 1.0;
        return Math.max(0.0, Math.min(1.0, recent / baseline));
    }

    /** 1 - coefficiente di variazione: alta se le partite si somigliano. */
    private double consistency(List<Double> xs) {
        double mean = mean(xs);
        if (mean <= 0.0) return 0.0;
        double cv = stdDev(xs, mean) / mean;
        return Math.max(0.0, Math.min(1.0, 1.0 - cv));
    }

    private String status(int energy, int games) {
        if (energy >= 70) return "alta — consistenza mantenuta (" + games + " game in sessione)";
        if (energy >= 40) return "in calo — la consistenza sta scendendo (" + games + " game)";
        return "bassa — performance molto variabile rispetto all'inizio (" + games + " game)";
    }

    private List<Double> sub(List<Double> xs, int from, int to) {
        return xs.subList(Math.max(0, from), Math.min(xs.size(), to));
    }

    private double mean(List<Double> xs) {
        if (xs.isEmpty()) return 0.0;
        double s = 0.0;
        for (double x : xs) s += x;
        return s / xs.size();
    }

    private double mean(double... xs) {
        double s = 0.0;
        for (double x : xs) s += x;
        return xs.length == 0 ? 0.0 : s / xs.length;
    }

    private double stdDev(List<Double> xs, double mean) {
        if (xs.size() < 2) return 0.0;
        double s = 0.0;
        for (double x : xs) s += (x - mean) * (x - mean);
        return Math.sqrt(s / xs.size());
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}
