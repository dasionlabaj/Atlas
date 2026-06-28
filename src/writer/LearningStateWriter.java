package writer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.learning.LearningState;

/**
 * Persiste la memoria interna. Salvare i pesi non è "stampare un output":
 * è ciò che permette al prossimo run di continuare. Per questo è un atto esplicito,
 * con un proprietario, non un effetto collaterale del Trainer.
 */
public class LearningStateWriter {

    public static final String DEFAULT_PATH = "learning-state.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void write(LearningState state, String path) {
        try {
            Path file = Path.of(path);
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, gson.toJson(state), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile scrivere la memoria in " + path, e);
        }
    }
}
