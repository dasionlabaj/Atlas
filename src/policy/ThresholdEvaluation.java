package policy;

import model.SignalColor;

/**
 * Esito della valutazione di una soglia, in piani che non si contaminano:
 *   color          — cosa mostrare (il semaforo)
 *   semantic       — cosa significa, in token verificabile (per il Coach futuro)
 *   green/yellow   — le soglie contro cui il valore è stato giudicato
 *   reason         — come si racconta, osservazione in lingua umana
 *
 * Il reason DESCRIVE il numero ("Muori più di quanto incidi"), non prescrive
 * cosa fare. Il consiglio è un altro livello (Coach), non vive qui.
 *
 * Le soglie viaggiano insieme all'esito perché il "cassetto" (il dettaglio
 * espandibile) deve poter mostrare contro cosa il valore è stato letto, senza
 * tornare a interrogare la policy. È dato osservato, non giudizio.
 */
public class ThresholdEvaluation {

    private final SignalColor color;
    private final ThresholdSemantic semantic;
    private final double greenThreshold;
    private final double yellowThreshold;
    private final String reason;

    public ThresholdEvaluation(SignalColor color, ThresholdSemantic semantic,
                               double greenThreshold, double yellowThreshold, String reason) {
        this.color           = color;
        this.semantic        = semantic;
        this.greenThreshold  = greenThreshold;
        this.yellowThreshold = yellowThreshold;
        this.reason          = reason;
    }

    public SignalColor getColor()          { return color; }
    public ThresholdSemantic getSemantic() { return semantic; }
    public double getGreenThreshold()      { return greenThreshold; }
    public double getYellowThreshold()     { return yellowThreshold; }
    public String getReason()              { return reason; }
}
