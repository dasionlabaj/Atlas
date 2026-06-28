package service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import model.MatchPerformance;

/**
 * Cache locale della cellula Riot: "osservare senza dimenticare".
 *
 * Memorizza, per ogni Riot ID, i FATTI grezzi dell'ultima partita vista
 * (current + previous). Solo dati: la lente (signals, context, score) si
 * ricalcola sempre al volo in {@link RiotAnalysisService}, non si conserva.
 * Così il VIEW non chiama Riot e il REFRESH scarica solo se c'è qualcosa di nuovo.
 */
public class RiotAnalysisCache {

    public static final String DEFAULT_PATH = "cache/riot-cell-cache.json";

    /** Voce di cache: i due fatti grezzi che bastano a ricostruire l'analisi. */
    public static class Entry {
        private MatchPerformance current;
        private MatchPerformance previous;   // può essere null

        Entry() {}   // per gson

        Entry(MatchPerformance current, MatchPerformance previous) {
            this.current  = current;
            this.previous = previous;
        }

        public MatchPerformance getCurrent()  { return current; }
        public MatchPerformance getPrevious() { return previous; }
    }

    private final Path path;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public RiotAnalysisCache() {
        this(Path.of(DEFAULT_PATH));
    }

    public RiotAnalysisCache(Path path) {
        this.path = path;
    }

    /** Ritorna la voce nota per quel Riot ID, o null se non c'è ancora. */
    public Entry get(String riotId) {
        return load().get(riotId);
    }

    public void put(String riotId, MatchPerformance current, MatchPerformance previous) {
        Map<String, Entry> all = load();
        all.put(riotId, new Entry(current, previous));
        save(all);
    }

    private Map<String, Entry> load() {
        if (!Files.exists(path)) {
            return new HashMap<>();
        }
        try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Map<String, Entry> m = gson.fromJson(r,
                new TypeToken<Map<String, Entry>>() {}.getType());
            return m != null ? m : new HashMap<>();
        } catch (IOException | RuntimeException e) {
            // Cache illeggibile o corrotta: non è un errore fatale, si riparte da vuoto.
            return new HashMap<>();
        }
    }

    private void save(Map<String, Entry> all) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(all, w);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossibile salvare la cache Riot: " + path, e);
        }
    }
}
