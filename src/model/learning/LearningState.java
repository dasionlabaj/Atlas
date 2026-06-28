package model.learning;

import java.util.List;

/**
 * La memoria interna di Atlas: ciò che conserva dentro, non ciò che dichiara fuori.
 *
 *   snapshot/current.json   → ciò che Atlas dichiara   (SnapshotXxx)
 *   learning-state.json     → ciò che Atlas conserva   (LearningState)
 *
 * Non è un output del training: è ciò che permette ad Atlas di *continuare* invece
 * di ricominciare. Per questo porta anche una provenienza minima: quando è stata
 * aggiornata e quante epoche di apprendimento ha attraversato in totale (il numero
 * cresce a ogni run — è la prova che Atlas non riparte da zero).
 *
 * Record di soli dati: nessuna logica di IO né di training qui dentro.
 */
public record LearningState(
        List<FeatureWeight> weights,
        double bias,
        double learningRate,
        String updatedAt,
        long totalEpochs
) {
    /** I valori dei pesi nell'ordine in cui sono memorizzati (per il percettrone). */
    public double[] weightVector() {
        double[] v = new double[weights.size()];
        for (int i = 0; i < weights.size(); i++) v[i] = weights.get(i).value();
        return v;
    }
}
