package reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GroundTruthReader {

    public List<GroundTruthEntry> read(String path) throws IOException {
        Path file = Path.of(path);
        if (!Files.exists(file)) return List.of();

        List<String> lines = Files.readAllLines(file);
        List<GroundTruthEntry> entries = new ArrayList<>();

        for (String line : lines) {
            if (line.isBlank() || line.startsWith("date")) continue;
            String[] f = line.split(";");
            if (f.length < 3) continue;
            entries.add(new GroundTruthEntry(f[0].trim(), f[1].trim(), f[2].trim()));
        }

        return entries;
    }
}
