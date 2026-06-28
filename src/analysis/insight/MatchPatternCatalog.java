package analysis.insight;

import java.util.List;

import model.SignalColor;
import model.StatSignal;

/**
 * Il catalogo dei pattern che Atlas sa riconoscere. Pochi e dichiarati:
 * aggiungere un pattern qui è un atto esplicito — una classe con un id, non una
 * if buttata nel motore. È così che la regola "meglio nessuna interpretazione
 * che una debole" diventa difficile da violare per distrazione.
 */
public final class MatchPatternCatalog {

    private MatchPatternCatalog() {}

    public static List<MatchPattern> defaults() {
        return List.of(
            new RisorseNonConvertite(),
            new VisioneTiEspone(),
            new PartitaSolida());
    }

    /** Economia creata, ma non tradotta in impatto. */
    static final class RisorseNonConvertite extends MatchPattern {
        public String id() { return "risorse-non-convertite"; }
        public String title() { return "Risorse non convertite"; }
        public String hypothesis() {
            return "Hai creato un vantaggio economico, ma non sei riuscito a convertirlo in combattimenti favorevoli.";
        }
        public boolean predicate(List<StatSignal> s) {
            StatSignal cs = signal(s, "CS/min");
            StatSignal kda = signal(s, "KDA");
            return cs != null && kda != null
                && cs.getColor() == SignalColor.GREEN && weak(kda);
        }
        protected List<StatSignal> basis(List<StatSignal> s) {
            return List.of(signal(s, "CS/min"), signal(s, "KDA"));
        }
    }

    /** Il resto regge, ma la visione quasi assente è il buco. */
    static final class VisioneTiEspone extends MatchPattern {
        public String id() { return "visione-ti-espone"; }
        public String title() { return "La visione ti espone"; }
        public String hypothesis() {
            return "Economia e combattimenti reggono, ma la visione quasi assente rende difficile evitare i pickoff e preparare gli obiettivi.";
        }
        public boolean predicate(List<StatSignal> s) {
            StatSignal vision = signal(s, "Vision/min");
            StatSignal cs = signal(s, "CS/min");
            return vision != null && cs != null
                && vision.getColor() == SignalColor.RED && cs.getColor() != SignalColor.RED;
        }
        protected List<StatSignal> basis(List<StatSignal> s) {
            return List.of(signal(s, "Vision/min"), signal(s, "CS/min"));
        }
    }

    /** Nessuna debolezza dominante. */
    static final class PartitaSolida extends MatchPattern {
        public String id() { return "partita-solida"; }
        public String title() { return "Partita solida"; }
        public String hypothesis() {
            return "Nessuna debolezza dominante: gli indicatori osservati sono tutti sopra soglia.";
        }
        public boolean predicate(List<StatSignal> s) {
            StatSignal cs = signal(s, "CS/min");
            StatSignal vision = signal(s, "Vision/min");
            StatSignal kda = signal(s, "KDA");
            return cs != null && vision != null && kda != null
                && cs.getColor() == SignalColor.GREEN
                && vision.getColor() == SignalColor.GREEN
                && kda.getColor() == SignalColor.GREEN;
        }
        protected List<StatSignal> basis(List<StatSignal> s) {
            return List.of(signal(s, "CS/min"), signal(s, "Vision/min"), signal(s, "KDA"));
        }
    }
}
