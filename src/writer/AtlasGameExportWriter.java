package writer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.export.AtlasGameExport;

/**
 * Serializza il contratto di esportazione in JSON, con lo stesso idioma dello
 * {@link SnapshotWriter} (Gson, pretty-printing, null omessi). Due gesti distinti:
 * {@link #toJson} per ottenere la stringa da incollare in una chat, {@link #write}
 * per salvarla come artefatto consultabile.
 */
public class AtlasGameExportWriter {

    public static final String DEFAULT_PATH = "export/last-game.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /** La stringa JSON pronta da incollare. Nessun IO. */
    public String toJson(AtlasGameExport export) {
        return gson.toJson(export);
    }

    /** Salva l'esportazione come artefatto e ritorna il path scritto. */
    public Path write(AtlasGameExport export, String path) {
        try {
            Path file = Path.of(path);
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, toJson(export), StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile scrivere l'esportazione in " + path, e);
        }
    }
}
