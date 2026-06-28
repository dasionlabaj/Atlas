package analysis.insight;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import model.StatSignal;
import model.insight.MatchInsight;

/**
 * Riconosce l'interpretazione principale di una partita: scorre i pattern
 * dichiarati nel catalogo, tiene quelli che si applicano, e sceglie il più
 * sostenuto dai dati. Se nessuno si applica, non dichiara nulla (Optional.empty):
 * Atlas non inventa un significato che non vede.
 *
 * Qui dentro non c'è logica di interpretazione: vive nei MatchPattern. Questo è
 * solo il riconoscitore. Il catalogo è iniettabile, così i pattern si possono
 * provare in isolamento.
 */
public class MatchInsightAnalyzer {

    private final List<MatchPattern> patterns;

    public MatchInsightAnalyzer() {
        this(MatchPatternCatalog.defaults());
    }

    public MatchInsightAnalyzer(List<MatchPattern> patterns) {
        this.patterns = patterns;
    }

    public Optional<MatchInsight> analyze(List<StatSignal> signals) {
        return patterns.stream()
            .filter(p -> p.predicate(signals))
            .map(p -> new MatchInsight(
                    p.id(), p.title(), p.hypothesis(), p.confidence(signals), p.evidence(signals)))
            .max(Comparator.comparingDouble(MatchInsight::confidence));
    }
}
