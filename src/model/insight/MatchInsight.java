package model.insight;

import java.util.List;

import model.Evidence;

/**
 * L'interpretazione di UNA partita nel suo insieme — la terza domanda di Atlas:
 * "che cosa significa questa partita?". Primo livello che mette in relazione
 * osservazioni diverse (gli StatSignal) invece di leggerle una a una.
 *
 * NON è un voto. È un'ipotesi:
 *   id              = l'id stabile del pattern riconosciuto ("risorse-non-convertite").
 *                     Parla al contratto: rende il titolo verificabile dall'esterno.
 *   title           = il nome breve dell'interpretazione. Parla al giocatore.
 *   hypothesis      = la lettura, sempre al condizionale ("potrebbe", mai "è")
 *   confidence      = 0-100, derivata dai segnali coinvolti, non scelta a mano
 *   involvedSignals = le evidenze: i segnali da cui nasce l'ipotesi (Evidence)
 *
 * Riusa Evidence: un solo vocabolario dell'evidenza in tutto Atlas.
 */
public record MatchInsight(
    String id,
    String title,
    String hypothesis,
    double confidence,
    List<Evidence> involvedSignals
) {}
