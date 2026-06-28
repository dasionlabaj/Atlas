package writer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.snapshot.AtlasSnapshot;
import model.snapshot.RiotAccountKey;

/**
 * Salva lo snapshot come artefatto consultabile, esattamente come i report.
 * Da quel momento la lente non dipende più dal motore vivo: legge un file.
 */
public class SnapshotWriter {

    public static final String DEFAULT_PATH = "snapshot/current.json";
    public static final String ACCOUNT_DIR  = "snapshot/account";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void write(AtlasSnapshot snapshot, String path) {
        try {
            Path file = Path.of(path);
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, gson.toJson(snapshot), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile scrivere lo snapshot in " + path, e);
        }
    }

    /** Scrive lo snapshot di uno specifico account in snapshot/account/&lt;segmento&gt;.json. */
    public void writeForAccount(RiotAccountKey accountKey, AtlasSnapshot snapshot) {
        write(snapshot, accountPath(accountKey));
    }

    /** Il path del file snapshot per un account, derivato dalla chiave url-safe. */
    public static String accountPath(RiotAccountKey accountKey) {
        return ACCOUNT_DIR + "/" + accountKey.toPathSegment() + ".json";
    }
}
