package writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import analysis.rank.RankPredictionResult;
import model.rank.Rank;

public class RankPredictionHistoryWriter {

    private static final String HEADER = "date;role;score;predictedRank;confidence;sampleSize";

    public void append(String path, String role, RankPredictionResult result) throws IOException {
        Path file = Path.of(path);
        Files.createDirectories(file.getParent());

        boolean needsHeader = !Files.exists(file) || Files.size(file) == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile(), true))) {
            if (needsHeader) {
                writer.write(HEADER);
                writer.newLine();
            }
            writer.write(buildLine(role, result));
            writer.newLine();
        }
    }

    private String buildLine(String role, RankPredictionResult result) {
        Rank rank = result.prediction().predictedRank();
        String rankLabel = rank.tier().name() + "_" + rank.division().name();
        return String.format("%s;%s;%.2f;%s;%.2f;%d",
            LocalDate.now(),
            role,
            result.score(),
            rankLabel,
            result.prediction().confidence(),
            result.inputFeatures().sampleSize()
        );
    }
}
