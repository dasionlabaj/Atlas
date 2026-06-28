package service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

import learning.PerceptronState;
import model.learning.FeatureWeight;
import model.learning.LearningState;
import reader.LearningStateReader;
import writer.LearningStateWriter;

/**
 * Cittadino di prima classe: la memoria interna di Atlas, esposta come servizio.
 *
 * È il confine domain-aware. Conosce i nomi delle feature (semantica di dominio)
 * e fa il ponte fra i due mondi:
 *   - verso il Trainer  → numeri puri (PerceptronState), domain-blind
 *   - verso la memoria  → pesi nominati (LearningState)
 *
 * load() incarna il principio "Atlas continua":
 *   1. se esiste learning-state.json compatibile → riparte da lì;
 *   2. se non esiste ma c'è il legacy perceptron-state.json compatibile → lo eredita
 *      (non si butta via ciò che Atlas aveva già imparato);
 *   3. se la dimensione non combacia (cambio di feature set) → reinizializza invece
 *      di crashare (legge di Origin: cambiare dominio senza distruggere il sistema);
 *   4. altrimenti → memoria nuova, pesi casuali.
 */
public class LearningStateService {

    private static final String LEGACY_PATH = "perceptron-state.json";
    private static final double DEFAULT_LEARNING_RATE = 0.01;

    private final LearningStateReader reader = new LearningStateReader();
    private final LearningStateWriter writer = new LearningStateWriter();
    private final Gson gson = new Gson();

    /** Carica la memoria compatibile con questo insieme di feature, o ne crea una. */
    public LearningState load(String[] featureNames) {
        LearningState current = reader.read(LearningStateWriter.DEFAULT_PATH);
        if (current != null && current.weights().size() == featureNames.length) {
            return current; // Atlas continua
        }
        if (current == null) {
            LearningState legacy = inheritLegacy(featureNames);
            if (legacy != null) return legacy;
        }
        return initial(featureNames); // memoria nuova (o feature set cambiato)
    }

    public void save(LearningState state) {
        writer.write(state, LearningStateWriter.DEFAULT_PATH);
    }

    // --- ponte verso il Trainer (numeri puri) ---

    public PerceptronState toPerceptronState(LearningState state) {
        PerceptronState ps = new PerceptronState();
        ps.weights = state.weightVector();
        ps.bias = state.bias();
        ps.learningRate = state.learningRate();
        return ps;
    }

    /** Eleva i numeri allenati a memoria nominata. Qui entrano i nomi, non nel Trainer. */
    public LearningState elevate(PerceptronState trained, String[] featureNames, long totalEpochs) {
        List<FeatureWeight> weights = new ArrayList<>(featureNames.length);
        for (int i = 0; i < featureNames.length; i++) {
            weights.add(new FeatureWeight(featureNames[i], trained.weights[i]));
        }
        return new LearningState(weights, trained.bias, trained.learningRate,
                Instant.now().toString(), totalEpochs);
    }

    // --- inizializzazione / eredità ---

    private LearningState inheritLegacy(String[] featureNames) {
        try {
            java.nio.file.Path legacy = java.nio.file.Path.of(LEGACY_PATH);
            if (!java.nio.file.Files.exists(legacy)) return null;
            String json = java.nio.file.Files.readString(legacy);
            PerceptronState ps = gson.fromJson(json, PerceptronState.class);
            if (ps == null || ps.weights == null || ps.weights.length != featureNames.length) return null;
            return elevate(ps, featureNames, 0L); // eredita i pesi, ma il conteggio epoche riparte
        } catch (Exception e) {
            return null; // il legacy è un bonus, non una dipendenza
        }
    }

    private LearningState initial(String[] featureNames) {
        Random random = new Random();
        List<FeatureWeight> weights = new ArrayList<>(featureNames.length);
        for (String name : featureNames) {
            weights.add(new FeatureWeight(name, random.nextDouble() * 2 - 1));
        }
        double bias = random.nextDouble() * 2 - 1;
        return new LearningState(weights, bias, DEFAULT_LEARNING_RATE, Instant.now().toString(), 0L);
    }
}
