package service;

import java.util.List;

import analysis.MatchAnalysisContext;
import model.MatchPerformance;
import model.StatSignal;

/**
 * Risultato verificabile dell'analisi dell'ultima partita.
 * Solo dati: niente IO, niente UI. È l'osservazione che la cellula Riot
 * consegna al mondo (CLI o finestra), senza esporre dettagli di Riot API.
 */
public class LastGameAnalysis {

    private final MatchPerformance current;
    private final MatchPerformance previous;   // può essere null se non c'è partita precedente
    private final MatchAnalysisContext context;
    private final List<StatSignal> signals;
    private final double score;

    public LastGameAnalysis(MatchPerformance current,
                            MatchPerformance previous,
                            MatchAnalysisContext context,
                            List<StatSignal> signals,
                            double score) {
        this.current  = current;
        this.previous = previous;
        this.context  = context;
        this.signals  = signals;
        this.score    = score;
    }

    public MatchPerformance getCurrent()     { return current; }
    public MatchPerformance getPrevious()    { return previous; }
    public MatchAnalysisContext getContext() { return context; }
    public List<StatSignal> getSignals()     { return signals; }
    public double getScore()                 { return score; }
}
