package perceptor.lol;

import model.MatchPerformance;
import perceptor.Sample;

public class LoLPerceptor {

    /**
     * I nomi delle feature, nell'ordine in cui observe() le produce.
     * Fonte di verità: i pesi appresi prendono il loro nome da qui.
     * Cambiare quest'ordine = cambiare il significato dei pesi salvati.
     */
    public static final String[] FEATURE_NAMES = {
        "KDA", "CS/min", "Vision/min", "Deaths", "DMG/min"
    };

    public Sample observe(MatchPerformance match) {
        double minutes     = match.getMinutes();
        double kda         = (match.getKills() + match.getAssists()) / Math.max(1.0, match.getDeaths());
        double csPerMin    = match.getCs() / minutes;
        double visionPerMin = match.getVisionScore() / minutes;
        double deaths      = match.getDeaths();
        double dmgPerMin   = match.getDamageDealt() / minutes / 1000.0;

        double[] features = { kda, csPerMin, visionPerMin, deaths, dmgPerMin };
        int expected = match.isWin() ? 1 : 0;
        return new Sample(features, expected, isTrainable(match));
    }

    private boolean isTrainable(MatchPerformance match) {
        if (match.getMinutes() < 10) return false;
        double csPerMin = match.getCs() / match.getMinutes();
        if (csPerMin < 3) return false;
        double kda = (match.getKills() + match.getAssists()) / Math.max(1.0, match.getDeaths());
        if (kda == 0 && match.getVisionScore() == 0) return false;
        return true;
    }
}
