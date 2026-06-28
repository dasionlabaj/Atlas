package reader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;

import model.snapshot.AtlasSnapshot;
import model.snapshot.SnapshotStatus;
import model.snapshot.SnapshotSummary;

/**
 * Legge lo snapshot consolidato dal file.
 *
 * Se nessun ciclo è ancora stato eseguito (file assente), non è un errore:
 * Atlas semplicemente non ha ancora osservato. Restituisce uno stato IDLE
 * vuoto ma valido, così la porta risponde anche prima della prima analisi.
 */
public class SnapshotReader {

    private final Gson gson = new Gson();

    public AtlasSnapshot read(String path) {
        Path file = Path.of(path);
        if (!Files.exists(file)) {
            return empty();
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            AtlasSnapshot snapshot = gson.fromJson(json, AtlasSnapshot.class);
            return snapshot != null ? snapshot : empty();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile leggere lo snapshot da " + path, e);
        }
    }

    private AtlasSnapshot empty() {
        return new AtlasSnapshot(
                new SnapshotSummary("—", SnapshotStatus.IDLE, null),
                List.of(),
                null,   // matchInsight — senza partita non c'è interpretazione
                List.of(),
                null,   // rankPrediction
                null,   // focusProfile
                null,   // sessionBattery
                null,   // lastUpdated
                AtlasSnapshot.CONTRACT_VERSION);
    }
}
