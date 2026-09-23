package pipeline;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import com.google.gson.JsonElement;
import learning.Perceptron;
import model.learning.LearningState;
import model.learning.TrainingRun;
import perceptor.Sample;
import service.LearningStateService;
import training.Trainer;

/** Feature -> modello -> risultato. TRAIN e chiusura sono gesti espliciti. */
public final class AiPipeline {
    public record Prediction(int label, long modelEpochs, PipelineEvent event) {}

    private final LearningStateService memory;
    private final Path logPath;

    public AiPipeline() { this(Path.of(".")); }

    /** Ogni directory identifica una memoria; le simulazioni usano una directory propria. */
    public AiPipeline(Path directory) {
        memory = new LearningStateService(directory);
        logPath = directory.resolve("learning-log.txt");
    }

    public synchronized Prediction predict(double[] features, String[] names, PipelineEvent previous) {
        Objects.requireNonNull(previous, "previous");
        PipelineInputs.names(names);
        PipelineInputs.vector(features, names.length);
        LearningState state = memory.loadForInference(names);
        var numeric = memory.toPerceptronState(state);
        PipelineInputs.model(numeric, names.length);
        int label = new Perceptron(numeric).predict(features);
        return new Prediction(label, state.totalEpochs(), previous.next(PipelineEvent.Kind.INFER));
    }

    public synchronized LearningState train(List<Sample> samples, String[] names,
                                             int epochs, PipelineEvent previous) throws IOException {
        Objects.requireNonNull(previous, "previous");
        PipelineInputs.names(names);
        long trainedCount = PipelineInputs.samples(samples, names.length, epochs);
        LearningState prior = memory.loadForTraining(names);
        long total = Math.addExact(prior.totalEpochs(), epochs);
        var numeric = memory.toPerceptronState(prior);
        PipelineInputs.model(numeric, names.length);
        var trained = new Trainer(samples, logPath).run(epochs, numeric);
        PipelineInputs.model(trained, names.length);
        LearningState next = memory.elevate(trained, names, total);
        var history = new java.util.ArrayList<>(prior.trainings());
        history.add(new TrainingRun(previous.next(PipelineEvent.Kind.TRAIN), epochs,
                Math.toIntExact(trainedCount), samples.size() - Math.toIntExact(trainedCount),
                total, next.updatedAt()));
        next = new LearningState(next.weights(), next.bias(), next.learningRate(),
                next.updatedAt(), total, history);
        memory.save(next); // pesi e catalogo: un unico checkpoint
        return next;
    }

    /** Solo al termine: l'oggetto JSON vuoto è il segnale IA del protocollo Atlas. */
    public PipelineEvent complete(JsonElement output, PipelineEvent previous) {
        Objects.requireNonNull(previous, "previous");
        if (output == null || output.isJsonNull())
            throw new IllegalArgumentException("Output finale assente: non è un segnale IA");
        boolean emptyObject = output.isJsonObject() && output.getAsJsonObject().size() == 0;
        return previous.next(emptyObject ? PipelineEvent.Kind.IA : PipelineEvent.Kind.RESULT);
    }
}
