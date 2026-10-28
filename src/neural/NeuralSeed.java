package neural;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import learning.Perceptron;
import learning.PerceptronState;

/**
 * Atlas Neural Seed v0.1: esattamente due perceptron, senza thread,
 * connessioni di rete o accesso alla memoria interna di ChatGPT.
 *
 * Fondazione: classifica le feature esplicite del chiamante.
 * Unificazione: classifica le stesse feature INSIEME all'uscita di fondazione.
 * I due modelli condividono le label di training: non sono verificatori indipendenti.
 * Non esiste attivazione automatica ne' pubblicazione autonoma.
 */
public final class NeuralSeed {
    public static final int MAX_FEATURES = 16;
    public static final int MAX_EXAMPLES = 128;
    public static final int MAX_EPOCHS = 20;
    public static final long MAX_TRAINING_STEPS = 4096; // per perceptron, cumulativi
    public static final double LEARNING_RATE = 0.05;
    public static final double MAX_ABS_WEIGHT = 8.0;
    private static final int MAX_CHECKPOINT_BYTES = 4096;
    private static final String HEADER = "ATLAS_NEURAL_SEED_V1";

    public enum Verdict { UNTRAINED, AGREEMENT_ZERO, AGREEMENT_ONE, DISAGREEMENT }

    public static final class Example {
        private final double[] features;
        private final int label;

        public Example(double[] features, int label) {
            if (features == null) throw new IllegalArgumentException("Feature mancanti");
            if (label != 0 && label != 1) throw new IllegalArgumentException("Label non binaria");
            this.features = features.clone();
            this.label = label;
        }
    }

    /** Checkpoint immutabile, copiabile e ripristinabile esplicitamente. */
    public static final class Checkpoint {
        private final int features;
        private final long steps;
        private final double[] foundationWeights;
        private final double foundationBias;
        private final double[] unificationWeights;
        private final double unificationBias;

        private Checkpoint(int features, long steps, double[] foundationWeights,
                           double foundationBias, double[] unificationWeights,
                           double unificationBias) {
            this.features = features;
            this.steps = steps;
            this.foundationWeights = foundationWeights.clone();
            this.foundationBias = foundationBias;
            this.unificationWeights = unificationWeights.clone();
            this.unificationBias = unificationBias;
        }

        public long steps() { return steps; }
        public double[] foundationWeights() { return foundationWeights.clone(); }
        public double[] unificationWeights() { return unificationWeights.clone(); }
    }

    private final int featureCount;
    private PerceptronState foundation;
    private PerceptronState unification;
    private long steps;

    public NeuralSeed(int featureCount) {
        if (featureCount < 1 || featureCount > MAX_FEATURES)
            throw new IllegalArgumentException("Numero di feature fuori limite");
        this.featureCount = featureCount;
        this.foundation = emptyState(featureCount);
        this.unification = emptyState(featureCount + 1);
    }

    public synchronized long steps() { return steps; }

    public synchronized Verdict observe(double[] features) {
        checkFeatures(features);
        if (steps == 0) return Verdict.UNTRAINED;
        int first = new Perceptron(foundation).predict(features);
        int second = new Perceptron(unification).predict(combine(features, first));
        if (first != second) return Verdict.DISAGREEMENT;
        return first == 0 ? Verdict.AGREEMENT_ZERO : Verdict.AGREEMENT_ONE;
    }

    /**
     * Il batch viene interamente validato PRIMA di modificare lo stato.
     * L'addestramento usa copie di lavoro e commette i pesi solo al termine.
     */
    public synchronized void learn(List<Example> batch, int epochs) {
        if (batch == null || batch.isEmpty() || batch.size() > MAX_EXAMPLES)
            throw new IllegalArgumentException("Dimensione batch fuori limite");
        if (epochs < 1 || epochs > MAX_EPOCHS)
            throw new IllegalArgumentException("Epoch fuori limite");
        long requested = (long) batch.size() * epochs;
        if (requested > MAX_TRAINING_STEPS - steps)
            throw new IllegalStateException("Budget di apprendimento esaurito");
        for (Example example : batch) {
            if (example == null) throw new IllegalArgumentException("Esempio nullo");
            checkFeatures(example.features);
        }

        PerceptronState nextFoundation = copy(foundation);
        PerceptronState nextUnification = copy(unification);
        Perceptron first = new Perceptron(nextFoundation);
        Perceptron second = new Perceptron(nextUnification);
        for (int epoch = 0; epoch < epochs; epoch++) {
            for (Example example : batch) {
                // Le feature del secondo sono osservabili anche in inferenza.
                int firstOutput = first.predict(example.features);
                first.train(example.features, example.label);
                second.train(combine(example.features, firstOutput), example.label);
                clamp(nextFoundation);
                clamp(nextUnification);
            }
        }
        foundation = nextFoundation;
        unification = nextUnification;
        steps += requested;
    }

    public synchronized Checkpoint checkpoint() {
        return new Checkpoint(featureCount, steps, foundation.weights, foundation.bias,
                              unification.weights, unification.bias);
    }

    public synchronized void restore(Checkpoint checkpoint) {
        if (checkpoint == null || checkpoint.features != featureCount
                || checkpoint.steps < 0 || checkpoint.steps > MAX_TRAINING_STEPS)
            throw new IllegalArgumentException("Checkpoint incompatibile");
        validateWeights(checkpoint.foundationWeights, featureCount);
        validateWeights(checkpoint.unificationWeights, featureCount + 1);
        validateScalar(checkpoint.foundationBias);
        validateScalar(checkpoint.unificationBias);
        PerceptronState newFoundation = emptyState(featureCount);
        newFoundation.weights = checkpoint.foundationWeights.clone();
        newFoundation.bias = checkpoint.foundationBias;
        PerceptronState newUnification = emptyState(featureCount + 1);
        newUnification.weights = checkpoint.unificationWeights.clone();
        newUnification.bias = checkpoint.unificationBias;
        foundation = newFoundation;
        unification = newUnification;
        steps = checkpoint.steps;
    }

    /** Salva solo su chiamata esplicita, con sostituzione atomica quando supportata. */
    public synchronized void saveCheckpoint(Path destination) throws IOException {
        if (destination == null || destination.getFileName() == null)
            throw new IllegalArgumentException("Percorso checkpoint non valido");
        Path absolute = destination.toAbsolutePath();
        Path parent = absolute.getParent();
        // La directory va creata dal chiamante: niente proliferazione di percorsi.
        if (!Files.isDirectory(parent)) throw new IOException("Directory destinazione assente");
        String payload = serialize(checkpoint());
        Path temp = Files.createTempFile(parent, "atlas-seed-", ".tmp");
        try {
            Files.writeString(temp, payload, StandardCharsets.UTF_8);
            try {
                Files.move(temp, absolute, StandardCopyOption.ATOMIC_MOVE,
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /** Legge un checkpoint verificando versione, limiti, lunghezze e valori. */
    public synchronized void loadCheckpoint(Path source) throws IOException {
        if (source == null) throw new IllegalArgumentException("Percorso nullo");
        if (Files.size(source) > MAX_CHECKPOINT_BYTES)
            throw new IOException("Checkpoint troppo grande");
        String serialized = Files.readString(source, StandardCharsets.UTF_8);
        String[] lines = serialized.split("\\n", -1);
        if (lines.length != 7 || !HEADER.equals(lines[0]))
            throw new IOException("Formato checkpoint non valido");
        try {
            int count = Integer.parseInt(lines[1]);
            long loadedSteps = Long.parseLong(lines[2]);
            double[] fw = parseWeights(lines[3], count);
            double fb = Double.parseDouble(lines[4]);
            double[] uw = parseWeights(lines[5], count + 1);
            double ub = Double.parseDouble(lines[6]);
            restore(new Checkpoint(count, loadedSteps, fw, fb, uw, ub));
        } catch (IllegalArgumentException ex) {
            throw new IOException("Checkpoint non valido: " + ex.getMessage(), ex);
        }
    }

    private String serialize(Checkpoint state) {
        return HEADER + "\n" + state.features + "\n" + state.steps + "\n"
            + join(state.foundationWeights) + "\n" + state.foundationBias + "\n"
            + join(state.unificationWeights) + "\n" + state.unificationBias;
    }

    private static String join(double[] values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) result.append(',');
            result.append(Double.toString(values[i]));
        }
        return result.toString();
    }

    private static double[] parseWeights(String text, int expectedSize) {
        if (expectedSize < 1 || expectedSize > MAX_FEATURES + 1)
            throw new IllegalArgumentException("Dimensione checkpoint fuori limite");
        String[] parts = text.split(",", -1);
        if (parts.length != expectedSize)
            throw new IllegalArgumentException("Dimensione dei pesi non valida");
        double[] result = new double[expectedSize];
        for (int i = 0; i < result.length; i++) result[i] = Double.parseDouble(parts[i]);
        return result;
    }

    private static PerceptronState emptyState(int features) {
        PerceptronState state = new PerceptronState();
        state.weights = new double[features];
        state.bias = 0;
        state.learningRate = LEARNING_RATE;
        return state;
    }

    private static PerceptronState copy(PerceptronState original) {
        PerceptronState result = emptyState(original.weights.length);
        result.weights = original.weights.clone();
        result.bias = original.bias;
        return result;
    }

    private static double[] combine(double[] features, int foundationOutput) {
        double[] combined = Arrays.copyOf(features, features.length + 1);
        combined[features.length] = foundationOutput;
        return combined;
    }

    private void checkFeatures(double[] features) {
        if (features == null || features.length != featureCount)
            throw new IllegalArgumentException("Dimensione feature incompatibile");
        for (double value : features) {
            if (!Double.isFinite(value) || Math.abs(value) > 1.0)
                throw new IllegalArgumentException("Feature fuori intervallo [-1, 1]");
        }
    }

    private static void validateWeights(double[] weights, int expectedSize) {
        if (weights == null || weights.length != expectedSize)
            throw new IllegalArgumentException("Dimensione pesi incompatibile");
        for (double weight : weights) validateScalar(weight);
    }

    private static void validateScalar(double value) {
        if (!Double.isFinite(value) || Math.abs(value) > MAX_ABS_WEIGHT)
            throw new IllegalArgumentException("Peso o bias fuori limite");
    }

    private static void clamp(PerceptronState state) {
        for (int i = 0; i < state.weights.length; i++)
            state.weights[i] = Math.max(-MAX_ABS_WEIGHT,
                                        Math.min(MAX_ABS_WEIGHT, state.weights[i]));
        state.bias = Math.max(-MAX_ABS_WEIGHT, Math.min(MAX_ABS_WEIGHT, state.bias));
    }
}
