package training;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import learning.Perceptron;
import learning.PerceptronState;
import perceptor.Sample;

public class Trainer {

    private static final String LOG_PATH = "learning-log.txt";

    private final List<Sample> samples;

    public Trainer(List<Sample> samples) {
        this.samples = samples;
    }

    /**
     * Allena a partire dallo stato fornito e RESTITUISCE lo stato aggiornato.
     * Non legge né scrive la memoria: l'IO è del LearningStateService/Writer.
     * Resta domain-blind: vede numeri (PerceptronState), mai nomi di feature.
     */
    public PerceptronState run(int epochs, PerceptronState state) throws IOException {
        int inputSize = samples.stream()
            .filter(s -> s.trainable)
            .findFirst()
            .map(s -> s.features.length)
            .orElseThrow(() -> new IllegalArgumentException("Nessun sample trainable"));
        if (state.weights.length != inputSize) {
            throw new IllegalArgumentException(
                "Stato incompatibile: " + state.weights.length + " pesi vs " + inputSize + " feature");
        }

        Perceptron perceptron = new Perceptron(state);

        try (BufferedWriter log = new BufferedWriter(new FileWriter(LOG_PATH, true))) {
            log.write("--- " + LocalDateTime.now() + " ---");
            log.newLine();

            for (int epoch = 1; epoch <= epochs; epoch++) {
                log.write("[epoch " + epoch + "]");
                log.newLine();

                int correct = 0;
                int trained = 0;
                int index   = 0;

                for (Sample sample : samples) {
                    index++;

                    if (!sample.trainable) {
                        log.write(formatSkip(index, sample));
                        log.newLine();
                        continue;
                    }

                    double[] wBefore   = Arrays.copyOf(state.weights, state.weights.length);
                    int prediction     = perceptron.train(sample.features, sample.expected);
                    int error          = sample.expected - prediction;
                    if (error == 0) correct++;
                    trained++;

                    log.write(formatTrained(index, sample, prediction, error, wBefore, state.weights));
                    log.newLine();
                }

                double accuracy = trained > 0 ? (double) correct / trained : 0.0;
                System.out.printf("  epoch %d  ->  correct: %d/%d  accuracy: %.1f%%%n",
                    epoch, correct, trained, accuracy * 100);
            }
        }

        return perceptron.state();
    }

    private static String formatSkip(int index, Sample sample) {
        StringBuilder sb = new StringBuilder();
        sb.append(index).append("|SKIP");
        for (double f : sample.features) sb.append(String.format(Locale.US, "|%.4f", f));
        sb.append("|-|-|-|-|-");
        return sb.toString();
    }

    private static String formatTrained(int index, Sample sample, int prediction, int error,
                                        double[] wBefore, double[] wAfter) {
        StringBuilder sb = new StringBuilder();
        sb.append(index).append("|").append(sample.expected == 1 ? "WIN" : "LOSS");
        for (double f : sample.features) sb.append(String.format(Locale.US, "|%.4f", f));
        sb.append(String.format("|%d|%d|%d", prediction, sample.expected, error));
        sb.append("|[");
        for (int i = 0; i < wBefore.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(Locale.US, "%.4f", wBefore[i]));
        }
        sb.append("]|[");
        for (int i = 0; i < wAfter.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(Locale.US, "%.4f", wAfter[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
