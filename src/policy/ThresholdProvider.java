package policy;

import model.SignalColor;

public class ThresholdProvider {

    public double getKdaGreen() {
        return 4.0;
    }

    public double getKdaYellow() {
        return 2.0;
    }

    public double getCsPerMinuteGreen() {
        return 7.0;
    }

    public double getCsPerMinuteYellow() {
        return 5.0;
    }

    public double getVisionPerMinuteGreen() {
        return 1.2;
    }

    public double getVisionPerMinuteYellow() {
        return 0.7;
    }

    /**
     * Valuta un valore per una metrica: dove cade rispetto alle soglie e come
     * lo si racconta. Qui vive la SEMANTICA della soglia, non solo i numeri.
     */
    public ThresholdEvaluation evaluate(Metric metric, double value) {
        double green  = greenOf(metric);
        double yellow = yellowOf(metric);

        SignalColor color;
        ThresholdSemantic semantic;
        if (value >= green) {
            color = SignalColor.GREEN;
            semantic = ThresholdSemantic.AT_OR_ABOVE_GREEN;
        } else if (value >= yellow) {
            color = SignalColor.YELLOW;
            semantic = ThresholdSemantic.BELOW_GREEN;
        } else {
            color = SignalColor.RED;
            semantic = ThresholdSemantic.BELOW_YELLOW;
        }
        return new ThresholdEvaluation(color, semantic, green, yellow, reasonFor(metric, semantic));
    }

    private double greenOf(Metric metric) {
        switch (metric) {
            case KDA:               return getKdaGreen();
            case CS_PER_MINUTE:     return getCsPerMinuteGreen();
            case VISION_PER_MINUTE: return getVisionPerMinuteGreen();
        }
        throw new IllegalArgumentException("Metric non gestita: " + metric);
    }

    private double yellowOf(Metric metric) {
        switch (metric) {
            case KDA:               return getKdaYellow();
            case CS_PER_MINUTE:     return getCsPerMinuteYellow();
            case VISION_PER_MINUTE: return getVisionPerMinuteYellow();
        }
        throw new IllegalArgumentException("Metric non gestita: " + metric);
    }

    // --- Semantica in lingua umana: descrive il numero, non prescrive l'azione. ---

    private String reasonFor(Metric metric, ThresholdSemantic semantic) {
        switch (metric) {
            case KDA:               return kdaReason(semantic);
            case CS_PER_MINUTE:     return csReason(semantic);
            case VISION_PER_MINUTE: return visionReason(semantic);
        }
        throw new IllegalArgumentException("Metric non gestita: " + metric);
    }

    private String kdaReason(ThresholdSemantic semantic) {
        switch (semantic) {
            case AT_OR_ABOVE_GREEN: return "Partecipi a molti più kill di quante volte muori.";
            case BELOW_GREEN:       return "Muori più di quanto incidi.";
            case BELOW_YELLOW:      return "Muori molto più di quanto incidi.";
        }
        throw new IllegalArgumentException("Semantic non gestita: " + semantic);
    }

    private String csReason(ThresholdSemantic semantic) {
        switch (semantic) {
            case AT_OR_ABOVE_GREEN: return "Raccogli molti minion al minuto.";
            case BELOW_GREEN:       return "Raccogli pochi minion al minuto.";
            case BELOW_YELLOW:      return "Raccogli pochissimi minion al minuto.";
        }
        throw new IllegalArgumentException("Semantic non gestita: " + semantic);
    }

    private String visionReason(ThresholdSemantic semantic) {
        switch (semantic) {
            case AT_OR_ABOVE_GREEN: return "Contribuisci molto alla visione.";
            case BELOW_GREEN:       return "Contribuisci poco alla visione.";
            case BELOW_YELLOW:      return "Contribuisci pochissimo alla visione.";
        }
        throw new IllegalArgumentException("Semantic non gestita: " + semantic);
    }
}
