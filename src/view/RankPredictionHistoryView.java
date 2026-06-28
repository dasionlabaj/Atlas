package view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RankPredictionHistoryView {

    public void show(String path, int lastN) throws IOException {
        Path file = Path.of(path);
        if (!Files.exists(file)) {
            System.out.println("--- rank history ---");
            System.out.println("(nessuno storico trovato)");
            System.out.println("--------------------");
            return;
        }

        List<String> lines = Files.readAllLines(file);
        // prima riga è header
        List<String> entries = lines.subList(Math.min(1, lines.size()), lines.size());
        List<String> slice = entries.subList(Math.max(0, entries.size() - lastN), entries.size());

        System.out.println("--- rank history ---");
        for (String line : slice) {
            System.out.println(formatLine(line));
        }
        System.out.println("--------------------");
    }

    private String formatLine(String csvLine) {
        String[] f = csvLine.split(";");
        if (f.length < 6) return csvLine;
        String date       = f[0];
        String role       = f[1];
        String score      = f[2];
        String rank       = f[3];
        String confidence = f[4];
        String sample     = f[5];
        double conf = Double.parseDouble(confidence.replace(",", "."));
        return String.format("%s | %s | %s | %s | %.0f%% | %s games",
            date, role, score, rank, conf * 100, sample);
    }
}
