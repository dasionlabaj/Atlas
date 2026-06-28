package reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.rank.Rank;

public class RankGoalReader {

    public Rank read(String path) throws IOException {
        Path file = Path.of(path);
        if (!Files.exists(file)) return null;
        List<String> lines = Files.readAllLines(file);
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("target_rank")) continue;
            return Rank.parse(line.trim());
        }
        return null;
    }
}
