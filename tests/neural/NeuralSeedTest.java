package neural;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Test eseguibile senza JUnit, con dati interamente sintetici. */
public final class NeuralSeedTest {
    private static void require(boolean condition, String reason) {
        if (!condition) throw new AssertionError(reason);
    }

    private static void mustFail(Runnable action) {
        boolean rejected = false;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "Validazione o budget non applicati");
    }

    public static void main(String[] args) throws Exception {
        NeuralSeed seed = new NeuralSeed(2);
        double[] positive = { 1, 1 };
        double[] negative = { -1, -1 };
        require(seed.observe(positive) == NeuralSeed.Verdict.UNTRAINED, "Deve iniziare non addestrato");
        List<NeuralSeed.Example> batch = List.of(
            new NeuralSeed.Example(positive, 1),
            new NeuralSeed.Example(negative, 0));
        NeuralSeed.Checkpoint initial = seed.checkpoint();
        seed.learn(batch, 12);
        require(seed.steps() == 24, "Numero step errato");
        require(seed.checkpoint().foundationWeights().length == 2, "Fondazione: due feature");
        require(seed.checkpoint().unificationWeights().length == 3,
                "Unificazione: due feature piu' uscita fondazione");
        require(seed.observe(positive) == NeuralSeed.Verdict.AGREEMENT_ONE, "Positivo");
        require(seed.observe(negative) == NeuralSeed.Verdict.AGREEMENT_ZERO, "Negativo");
        mustFail(() -> seed.learn(List.of(new NeuralSeed.Example(new double[]{ Double.NaN, 0 }, 1)), 1));
        mustFail(() -> seed.learn(batch, NeuralSeed.MAX_EPOCHS + 1));
        require(seed.steps() == 24, "Batch non valido deve lasciare invariato lo stato");

        Path directory = Files.createTempDirectory("atlas-seed-test-");
        Path saved = directory.resolve("checkpoint.txt");
        try {
            seed.saveCheckpoint(saved);
            NeuralSeed restored = new NeuralSeed(2);
            restored.loadCheckpoint(saved);
            require(restored.steps() == seed.steps(), "Persistenza step");
            require(restored.observe(positive) == seed.observe(positive), "Persistenza previsione");
            restored.restore(initial);
            require(restored.steps() == 0 && restored.observe(positive) == NeuralSeed.Verdict.UNTRAINED,
                    "Ripristino checkpoint iniziale");
            Files.writeString(saved, "non valido", StandardCharsets.UTF_8);
            boolean rejected = false;
            try { restored.loadCheckpoint(saved); } catch (java.io.IOException expected) { rejected = true; }
            require(rejected && restored.steps() == 0, "Checkpoint corrotto non cambia lo stato");
        } finally {
            Files.deleteIfExists(saved);
            Files.deleteIfExists(directory);
        }

        NeuralSeed budget = new NeuralSeed(1);
        List<NeuralSeed.Example> many = java.util.Collections.nCopies(128,
            new NeuralSeed.Example(new double[]{ 1 }, 1));
        budget.learn(many, 20); // 2560 step
        budget.learn(many, 12); // 4096 step
        require(budget.steps() == 4096, "Budget totale");
        mustFail(() -> budget.learn(batch.subList(0, 1), 1));
        System.out.println("PASS: 2 perceptron, fondazione->unificazione, limiti, checkpoint e ripristino");
    }
}
