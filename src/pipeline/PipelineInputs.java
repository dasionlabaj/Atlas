package pipeline;

import java.util.HashSet;
import java.util.List;
import learning.PerceptronState;
import perceptor.Sample;

final class PipelineInputs {
    private PipelineInputs() {}

    static void names(String[] names) {
        if (names == null || names.length == 0)
            throw new IllegalArgumentException("Servono nomi di feature");
        var seen = new HashSet<String>();
        for (String name : names)
            if (name == null || name.isBlank() || !seen.add(name))
                throw new IllegalArgumentException("Nomi di feature vuoti o duplicati");
    }

    static void vector(double[] values, int size) {
        if (values == null || values.length != size)
            throw new IllegalArgumentException("Dimensione delle feature incompatibile");
        for (double value : values)
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Feature non finita");
    }

    static void model(PerceptronState state, int size) {
        vector(state.weights, size);
        if (!Double.isFinite(state.bias) || !Double.isFinite(state.learningRate)
                || state.learningRate <= 0)
            throw new IllegalArgumentException("Stato del modello non valido");
    }

    static long samples(List<Sample> samples, int size, int epochs) {
        if (epochs <= 0 || samples == null || samples.isEmpty())
            throw new IllegalArgumentException("Servono campioni ed epoche positive");
        for (Sample sample : samples) {
            if (sample == null) throw new IllegalArgumentException("Campione nullo");
            vector(sample.features, size);
            if (sample.trainable && sample.expected != 0 && sample.expected != 1)
                throw new IllegalArgumentException("Il percettrone accetta etichette 0 o 1");
        }
        long count = samples.stream().filter(sample -> sample.trainable).count();
        if (count == 0) throw new IllegalArgumentException("Nessun campione allenabile");
        return count;
    }
}
