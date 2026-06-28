package writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import model.queue.PreQueueSnapshot;

/**
 * Salva lo stesso evento pre-coda in due viste:
 *  - writeText → diario umano (.txt), leggibile da te
 *  - writeJson → fascicolo strutturale (.json), rileggibile da Atlas
 * Stesso writer perché è lo stesso evento; nessuna delle due finge di essere l'altra.
 */
public class PreQueueSnapshotWriter {

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // LocalDateTime come stringa ISO: niente reflection nei tipi java.time (vietata
    // dal modulo java.base), e un valore che il futuro PreQueueSnapshotReader
    // potrà rileggere senza interpretare parole.
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .create();

    /** Diario umano. */
    public Path writeText(PreQueueSnapshot snapshot, Path path) throws IOException {
        return write(path, render(snapshot));
    }

    /** Fascicolo strutturale per Atlas. */
    public Path writeJson(PreQueueSnapshot snapshot, Path path) throws IOException {
        return write(path, GSON.toJson(snapshot));
    }

    private Path write(Path path, String content) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private String render(PreQueueSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PRE QUEUE ===\n");
        sb.append("Creato            : ").append(snapshot.createdAt().format(STAMP)).append('\n');
        sb.append("Promemoria        : ").append(snapshot.reminder()).append('\n');
        sb.append('\n');
        sb.append("Pensiero sul rank : ").append(snapshot.rankThought()).append('\n');
        sb.append("Freni dichiarati  : ").append(snapshot.blockers()).append('\n');

        if (snapshot.window() != null) {
            sb.append('\n');
            sb.append("Finestra di gioco\n");
            sb.append("- Inizio        : ").append(snapshot.window().start()).append('\n');
            sb.append("- Fine prevista : ").append(snapshot.window().expectedEnd()).append('\n');
            sb.append("- Trigger       : ").append(snapshot.window().trigger()).append('\n');
            sb.append("- Nota          : ").append(snapshot.window().note()).append('\n');
        }
        sb.append("=================\n");
        return sb.toString();
    }
}
