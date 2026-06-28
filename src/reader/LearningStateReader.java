package reader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;

import model.learning.LearningState;

/**
 * Ricarica la memoria interna dal file. Se non esiste ancora, restituisce null:
 * non è un errore, è un Atlas che non ha ancora imparato nulla. Decidere cosa fare
 * in quel caso (inizializzare) spetta al servizio, non al reader.
 */
public class LearningStateReader {

    private final Gson gson = new Gson();

    public LearningState read(String path) {
        Path file = Path.of(path);
        if (!Files.exists(file)) {
            return null;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            return gson.fromJson(json, LearningState.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile leggere la memoria da " + path, e);
        }
    }
}
