package core;

import java.io.IOException;

import service.LastGameAnalysis;

/**
 * La porta interna di Atlas. Non è HTTP: è la superficie di <em>capacità</em>
 * che il nucleo espone ai suoi consumatori (UI, CLI).
 *
 * Tre gesti, mai confusi: VIEW (mostrami ciò che sai), REFRESH (guarda se il
 * mondo è cambiato), TRAIN (modifica il modello). Qui esistono i primi due; il
 * TRAIN arriverà solo quando si saprà cosa deve imparare.
 *
 * Le app non chiamano i servizi direttamente: chiedono una capacità al nucleo,
 * e il nucleo instrada al figlio giusto. La UI conosce questa porta, non
 * RiotAnalysisService né RiotApiClient.
 */
public interface AtlasCoreApi {

    /**
     * VIEW: l'ultima osservazione Riot nota, senza chiamare Riot.
     * Ritorna null se non c'è ancora nulla di noto per quel Riot ID.
     */
    LastGameAnalysis getLastRiotAnalysis(String riotId);

    /**
     * REFRESH: controlla Riot e aggiorna lo stato noto solo se c'è una partita
     * nuova. Non allena nulla: "controlla se c'è una nuova osservazione".
     */
    LastGameAnalysis refreshLastRiotAnalysis(String riotId) throws IOException;
}
