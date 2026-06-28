package service;

import java.io.IOException;
import java.util.List;

import analysis.BreakDetector;
import analysis.MatchAnalysisContext;
import analysis.MatchAnalyzer;
import config.AtlasConfig;
import datasource.riot.RiotApiClient;
import model.MatchPerformance;
import model.Observation;
import model.ObservationType;
import model.StatSignal;
import policy.ThresholdProvider;

/**
 * La cellula Riot. Due gesti distinti, mai confusi:
 *
 *   VIEW    — {@link #getLast(String)}:     mostra ciò che già sa, senza toccare Riot.
 *   REFRESH — {@link #refreshLast(String)}: guarda se il mondo è cambiato, scarica
 *             solo se c'è una partita nuova, e ricorda (cache).
 *
 * Il TRAIN (modificare il modello) è un terzo gesto e qui non esiste: la cellula
 * osserva senza dimenticare, non impara.
 *
 * La lente (signals, context, score) si ricalcola sempre da {@code assemble}: la
 * cache conserva solo i fatti grezzi (MatchPerformance), non i giudizi.
 */
public class RiotAnalysisService {

    private final RiotApiClient riot;
    private final MatchAnalyzer analyzer;
    private final RiotAnalysisCache cache;

    /** Costruttore di comodo: legge la chiave da atlas.properties, cache di default. */
    public RiotAnalysisService() {
        this(new RiotApiClient(AtlasConfig.riotApiKey()),
             new MatchAnalyzer(new ThresholdProvider()),
             new RiotAnalysisCache());
    }

    public RiotAnalysisService(RiotApiClient riot, MatchAnalyzer analyzer) {
        this(riot, analyzer, new RiotAnalysisCache());
    }

    public RiotAnalysisService(RiotApiClient riot, MatchAnalyzer analyzer, RiotAnalysisCache cache) {
        this.riot     = riot;
        this.analyzer = analyzer;
        this.cache    = cache;
    }

    /**
     * VIEW: l'ultima osservazione nota, ricostruita dai fatti in cache.
     * Nessuna chiamata a Riot. Ritorna null se non c'è ancora nulla di noto.
     */
    public LastGameAnalysis getLast(String riotId) {
        RiotAnalysisCache.Entry e = cache.get(normalize(riotId));
        if (e == null || e.getCurrent() == null) {
            return null;
        }
        return assemble(e.getCurrent(), e.getPrevious());
    }

    /**
     * REFRESH: controlla Riot. Decide qui — non nel nucleo — se serve scaricare:
     * se l'ultimo matchId è quello che già conosco, non riscarico nulla e
     * restituisco ciò che so. Se è diverso, scarico la partita nuova e aggiorno
     * la cache. È "controlla se c'è una nuova osservazione", non "allenati".
     */
    public LastGameAnalysis refreshLast(String riotId) throws IOException {
        String key = normalize(riotId);
        String[] parts = splitId(riotId);

        String puuid = riot.getPuuid(parts[0], parts[1]);
        List<String> matchIds = riot.getMatchIds(puuid, 2);
        if (matchIds.isEmpty()) {
            throw new IllegalStateException("Nessuna partita trovata per " + riotId);
        }

        RiotAnalysisCache.Entry cached = cache.get(key);
        boolean unchanged = cached != null
                && cached.getCurrent() != null
                && matchIds.get(0).equals(cached.getCurrent().getMatchId());
        if (unchanged) {
            // Il mondo non è cambiato: nessun download.
            return assemble(cached.getCurrent(), cached.getPrevious());
        }

        MatchPerformance current  = riot.getMatch(matchIds.get(0), puuid);
        MatchPerformance previous = matchIds.size() > 1 ? riot.getMatch(matchIds.get(1), puuid) : null;
        cache.put(key, current, previous);
        return assemble(current, previous);
    }

    /** Ricostruisce la lente dai fatti grezzi. Pura: nessun IO, nessun effetto. */
    private LastGameAnalysis assemble(MatchPerformance current, MatchPerformance previous) {
        MatchAnalysisContext context = BreakDetector.detect(current, previous);
        List<StatSignal> signals = analyzer.analyze(current);
        double score = Observation
            .from(signals, current.getMatchId(), ObservationType.GIOCO)
            .getScore();
        return new LastGameAnalysis(current, previous, context, signals, score);
    }

    private static String normalize(String riotId) {
        return riotId == null ? "" : riotId.trim();
    }

    private static String[] splitId(String riotId) {
        String[] idParts = normalize(riotId).split("#", 2);
        if (idParts.length < 2 || idParts[0].isBlank() || idParts[1].isBlank()) {
            throw new IllegalArgumentException(
                "Riot ID non valido: serve il formato nome#tag (es. daxs#EUW)");
        }
        return new String[] { idParts[0].trim(), idParts[1].trim() };
    }
}
