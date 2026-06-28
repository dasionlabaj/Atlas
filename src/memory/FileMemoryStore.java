package memory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import model.Observation;

public class FileMemoryStore implements MemoryStore {

    private static final String PATH = "atlas-memory.txt";

    @Override
    public void save(Observation observation) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(PATH, true));

        writer.write(String.format(
                Locale.US,
                "%d|%s|%s|%s|%.4f",
                observation.getTimestamp(),
                observation.getSource(),
                observation.getMatchId(),
                observation.getType(),
                observation.getScore()
        ));
        writer.newLine();

        writer.close();
    }
}
