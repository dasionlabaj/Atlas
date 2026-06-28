package model.export;

import java.util.List;

/**
 * Il contratto di esportazione: ciò che Atlas sa già dell'ultima partita, ridotto
 * a un pacchetto stabile e incollabile altrove (ChatGPT, appunti), non alla UI.
 *
 *   snapshot/current.json  → ciò che Atlas dichiara alla propria lente (UI/REST)
 *   AtlasGameExport        → ciò che Atlas consegna per continuare il ragionamento
 *
 * Non è un'analisi nuova: è una proiezione di LastGameAnalysis, esattamente come
 * AtlasSnapshot è una proiezione dello stato consolidato. Qui non si interpreta
 * nulla — si traduce in lingua pubblica ciò che il motore ha già prodotto.
 *
 * Record di soli dati: nessuna logica, nessun IO. Come per lo snapshot, i campi
 * null spariscono dal JSON: Atlas non dichiara ciò che non sa.
 */
public record AtlasGameExport(
        String summoner,
        ExportMatch match,
        ExportContext context,
        double score,
        List<ExportSignal> signals,
        String exportedAt,
        int version
) {
    /** La versione del contratto. Cambia solo quando cambia la forma del pacchetto. */
    public static final int CONTRACT_VERSION = 1;

    /** L'identità e i fatti grezzi della partita: cosa è successo, non cosa significa. */
    public record ExportMatch(
            String matchId,
            String playedAt,           // ISO-8601, derivato dal timestamp Riot
            String champion,
            String role,
            String result,             // "WIN" | "LOSS"
            double durationMinutes,
            int kills,
            int deaths,
            int assists
    ) {}

    /** Il contesto in cui Atlas ha letto la partita (es. ritorno dopo una pausa). */
    public record ExportContext(
            String type,               // "NORMAL" | "RETURNING_AFTER_BREAK"
            Long hoursSinceLastGame    // null quando NORMAL: sparisce dal JSON
    ) {}

    /** La lettura di Atlas su un singolo numero: semaforo + soglie + osservazione. */
    public record ExportSignal(
            String name,
            String level,              // "GREEN" | "YELLOW" | "RED"
            double value,
            String reason,
            double greenThreshold,
            double yellowThreshold
    ) {}
}
