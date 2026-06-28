package analysis.insight;

import java.util.ArrayList;
import java.util.List;

import model.Evidence;
import model.SignalColor;
import model.StatSignal;

/**
 * Un pattern dichiarato: una forma di partita che Atlas sa riconoscere.
 *
 *   Atlas non inventa interpretazioni. Atlas riconosce pattern dichiarati.
 *
 * id          = nome stabile e verificabile del pattern (non prosa)
 * title       = il nome pubblico ("Risorse non convertite")
 * hypothesis  = la lettura, al condizionale
 * predicate   = quando il pattern si applica, sui segnali della partita
 * basis       = i segnali che lo costruiscono
 *
 * confidence ed evidence NON si reinventano pattern per pattern: vivono qui, una
 * volta sola. La disciplina del Portone B è strutturale, non buona volontà.
 */
public abstract class MatchPattern {

    public abstract String id();
    public abstract String title();
    public abstract String hypothesis();

    /** Quando il pattern si applica. */
    public abstract boolean predicate(List<StatSignal> signals);

    /** I segnali da cui nascono evidenza e confidenza. */
    protected abstract List<StatSignal> basis(List<StatSignal> signals);

    /** Confidenza = media della "chiarezza" dei segnali coinvolti (0-100). Condivisa. */
    public final double confidence(List<StatSignal> signals) {
        List<StatSignal> b = basis(signals);
        if (b.isEmpty()) return 0.0;
        double sum = 0.0;
        for (StatSignal s : b) sum += clarity(s);
        return (sum / b.size()) * 100.0;
    }

    /** Le evidenze: i segnali coinvolti, con fonte e colore leggibile. Condivisa. */
    public final List<Evidence> evidence(List<StatSignal> signals) {
        List<Evidence> out = new ArrayList<>();
        for (StatSignal s : basis(signals)) out.add(new Evidence(s.getName(), colorWord(s.getColor())));
        return out;
    }

    /** Quanto un segnale sta DECISO nella sua banda: lontano dalla soglia = più chiaro. */
    protected static double clarity(StatSignal s) {
        double green = s.getGreenThreshold();
        double yellow = s.getYellowThreshold();
        double v = s.getValue();
        double c;
        switch (s.getColor()) {
            case GREEN:  c = (v - green) / green;            break; // quanto sopra il verde
            case YELLOW: c = (green - v) / (green - yellow); break; // quanto lontano dal verde
            case RED:    c = (yellow - v) / yellow;          break; // quanto sotto il giallo
            default:     c = 0.0;
        }
        return Math.max(0.0, Math.min(1.0, c));
    }

    protected static boolean weak(StatSignal s) {
        return s.getColor() == SignalColor.YELLOW || s.getColor() == SignalColor.RED;
    }

    protected static String colorWord(SignalColor color) {
        switch (color) {
            case GREEN:  return "verde";
            case YELLOW: return "giallo";
            case RED:    return "rosso";
        }
        return "";
    }

    /** Trova un segnale per nome, o null se assente. */
    protected static StatSignal signal(List<StatSignal> signals, String name) {
        for (StatSignal s : signals) {
            if (name.equals(s.getName())) return s;
        }
        return null;
    }
}
