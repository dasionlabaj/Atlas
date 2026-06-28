package core;

import java.io.IOException;

import service.LastGameAnalysis;
import service.RiotAnalysisService;

/**
 * Il nucleo: instrada le capacità verso i servizi (le cellule figlie).
 * Conosce i figli; le app non li conoscono. Resta sottile — solo routing,
 * nessuna logica: la decisione "serve scaricare?" vive nella cellula, che
 * conosce il mondo e la sua cache.
 */
public class AtlasCore implements AtlasCoreApi {

    private final RiotAnalysisService riotAnalysisService;

    /** Costruttore di comodo: cabla la cellula Riot di default. */
    public AtlasCore() {
        this(new RiotAnalysisService());
    }

    /** Costruttore esplicito: riusa una cellula già costruita (es. da Main). */
    public AtlasCore(RiotAnalysisService riotAnalysisService) {
        this.riotAnalysisService = riotAnalysisService;
    }

    @Override
    public LastGameAnalysis getLastRiotAnalysis(String riotId) {
        return riotAnalysisService.getLast(riotId);
    }

    @Override
    public LastGameAnalysis refreshLastRiotAnalysis(String riotId) throws IOException {
        return riotAnalysisService.refreshLast(riotId);
    }
}
