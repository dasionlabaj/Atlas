package memory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Observation;
import model.ObservationType;

public class FileMemoryReader implements MemoryReader {

    private static final String PATH = "atlas-memory.txt";

    @Override
    public List<Observation> readAll() throws IOException {
        List<Observation> observations = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(PATH));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) continue;

            String[] parts = line.split("\\|");

            if (parts.length != 5) continue;

            long timestamp = Long.parseLong(parts[0]);
            String source = parts[1];
            String matchId = parts[2];
            ObservationType type = ObservationType.valueOf(parts[3]);
            double score = Double.parseDouble(parts[4]);

            observations.add(Observation.fromPersisted(timestamp, source, matchId, type, score));
        }

        reader.close();

        return observations;
    }
}
