package reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LearningLogReader {

    public LearningLogSummary read(String path) throws IOException {
        int trained   = 0;
        int skipped   = 0;
        int correct   = 0;
        double[] weightFirst = null;
        double[] weightLast  = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("---") || line.startsWith("[epoch")) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;

                String result = parts[1];

                if ("SKIP".equals(result)) {
                    skipped++;
                    continue;
                }

                if (parts.length < 8) continue;  // min: index|label|f0|pred|exp|err|wBefore|wAfter

                int last = parts.length - 1;
                double[] wAfter  = parseWeights(parts[last]);
                double[] wBefore = parseWeights(parts[last - 1]);
                int error = Integer.parseInt(parts[last - 2]);
                if (error == 0) correct++;
                trained++;

                if (weightFirst == null) weightFirst = wBefore;
                weightLast = wAfter;
            }
        }

        return new LearningLogSummary(trained, skipped, correct, weightFirst, weightLast);
    }

    private static double[] parseWeights(String s) {
        s = s.substring(1, s.length() - 1);
        String[] parts = s.split(",");
        double[] w = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            w[i] = Double.parseDouble(parts[i].trim());
        }
        return w;
    }
}
