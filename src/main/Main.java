package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import analysis.BreakDetector;
import analysis.MatchAnalysisContext;
import analysis.MatchAnalyzer;
import analysis.rank.RankFeatureVector;
import analysis.rank.RankFeatureVectorBuilder;
import analysis.rank.RankPredictionEngine;
import analysis.rank.RankPredictionResult;
import config.AtlasConfig;
import core.AtlasCore;
import core.AtlasCoreApi;
import model.queue.PreQueueSnapshot;
import reader.PreQueueConsoleReader;
import view.CoachProfileView;
import view.PreQueueSnapshotView;
import writer.PreQueueSnapshotWriter;
import view.GroundTruthView;
import view.RankGapView;
import view.RankPredictionHistoryView;
import view.RankPredictionView;
import reader.GroundTruthEntry;
import reader.GroundTruthReader;
import analysis.coach.CoachProfileAnalyzer;
import analysis.rank.RankGapAnalyzer;
import analysis.rank.RankGoalAnalyzer;
import analysis.rank.RankGoalMatchEstimator;
import model.rank.Rank;
import model.rank.RankGap;
import model.rank.RankGoalMatchEstimate;
import model.rank.RankGoalProgress;
import reader.RankGoalReader;
import view.RankGoalView;
import writer.RankPredictionHistoryWriter;
import datasource.riot.RiotApiClient;
import memory.FileMemoryStore;
import memory.MemoryStore;
import model.MatchPerformance;
import model.Observation;
import model.ObservationType;
import model.StatSignal;
import perceptor.Sample;
import perceptor.lol.LoLPerceptor;
import perceptor.test.TestEncoder;
import policy.ThresholdProvider;
import reader.LearningLogReader;
import reader.LearningLogSummary;
import resonance.model.MatchMemory;
import resonance.writer.MatchMemoryWriter;
import model.export.AtlasGameExport;
import service.AtlasGameExportBuilder;
import service.LastGameAnalysis;
import service.RiotAnalysisService;
import writer.AtlasGameExportWriter;
import training.Trainer;

public class Main {


    private static void reset() {
        new java.io.File("perceptron-state.json").delete();
        new java.io.File("learning-state.json").delete();
        new java.io.File("learning-log.txt").delete();
        System.out.println("Reset: learning-state.json, perceptron-state.json e learning-log.txt cancellati.");
    }

    private static void runTest() throws IOException {
        reset();
        List<Sample> samples = new TestEncoder(42L).generate(50);
        System.out.println("[test] 50 sample sintetici — 2 feature — regola: f0+f1 > 1.0 → WIN");
        System.out.println("\n--- learning ---");
        // Percorso di test: feature anonime (il TestEncoder non è un dominio).
        String[] testFeatures = { "t0", "t1" };
        service.LearningStateService learningStore = new service.LearningStateService();
        model.learning.LearningState prior = learningStore.load(testFeatures);
        learning.PerceptronState trained =
            new Trainer(samples).run(20, learningStore.toPerceptronState(prior));
        learningStore.save(learningStore.elevate(trained, testFeatures, prior.totalEpochs() + 20));
        System.out.println("\n--- summary ---");
        LearningLogSummary summary = new LearningLogReader().read("learning-log.txt");
        System.out.print(summary);
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "--test".equals(args[0])) {
            runTest();
            return;
        }
        if (args.length > 0 && "--serve".equals(args[0])) {
            int port = args.length > 1 ? Integer.parseInt(args[1]) : api.AtlasHttpServer.DEFAULT_PORT;
            new api.AtlasHttpServer(port).start();
            return; // il server resta in ascolto; non prosegue con la pipeline
        }
        if (args.length > 0 && "--prequeue".equals(args[0])) {
            runPreQueue();
            return; // slice isolato: registra il pre-coda, non tocca la pipeline Riot
        }
        if (args.length > 0 && "--reset".equals(args[0])) {
            reset();
        }

        String riotId = AtlasConfig.RIOT_ID;
        if (args.length > 0 && !args[0].startsWith("--")) {
            riotId = args[0];
        }
        String[] idParts = riotId.split("#", 2);
        String gameName = idParts[0];
        String tagLine  = idParts[1];

        boolean lastOnly = false;
        boolean exportOnly = false;
        for (String arg : args) {
            if ("--last".equals(arg)) { lastOnly = true; }
            if ("--export".equals(arg)) { exportOnly = true; }
        }

        RiotApiClient riot = new RiotApiClient(AtlasConfig.riotApiKey());
        MatchAnalyzer analyzer = new MatchAnalyzer(new ThresholdProvider());
        MemoryStore memory = new FileMemoryStore();
        LoLPerceptor perceptor = new LoLPerceptor();
        MatchMemoryWriter memoryWriter = new MatchMemoryWriter();

        // --last: stesso cuore della finestra Swing. Fetch + analisi vivono in
        // RiotAnalysisService; qui restano solo persistenza e presentazione testuale.
        if (lastOnly) {
            AtlasCoreApi core = new AtlasCore(new RiotAnalysisService(riot, analyzer));
            LastGameAnalysis analysis = core.refreshLastRiotAnalysis(riotId);
            MatchPerformance current = analysis.getCurrent();
            MatchAnalysisContext context = analysis.getContext();
            String contextNote = context.isReturningAfterBreak()
                ? "Partita dopo lunga pausa. Possibile ruggine su timing, mani, automatismi."
                : null;
            Observation obs = Observation.from(
                analysis.getSignals(), current.getMatchId(), ObservationType.GIOCO);
            memory.save(obs);
            String memPath = memoryWriter.write(MatchMemory.skeleton(
                current.getMatchId(), current.getChampion(), current.getRole(),
                current.isWin(), contextNote));
            String report = buildLastGameText(current, context, analysis.getScore(), memPath);
            System.out.print(report);
            saveLastGameReport(report);
            return;
        }

        // --export: stessa osservazione di --last, ma destinata ad altri strumenti.
        // Riot → Atlas → LastGameAnalysis → AtlasGameExport → JSON (stdout + artefatto).
        // Nessuna analisi qui: il builder proietta ciò che il core ha già prodotto.
        if (exportOnly) {
            AtlasCoreApi core = new AtlasCore(new RiotAnalysisService(riot, analyzer));
            LastGameAnalysis analysis = core.refreshLastRiotAnalysis(riotId);
            AtlasGameExport export = new AtlasGameExportBuilder().build(riotId, analysis);
            AtlasGameExportWriter exportWriter = new AtlasGameExportWriter();
            System.out.println(exportWriter.toJson(export));
            Path saved = exportWriter.write(export, AtlasGameExportWriter.DEFAULT_PATH);
            System.out.println("\nExport   : " + saved);
            return;
        }

        String puuid = riot.getPuuid(gameName, tagLine);
        System.out.println("PUUID: " + puuid);

        List<String> matchIds = riot.getMatchIds(puuid, 50);
        System.out.println("Match trovate: " + matchIds.size());

        List<MatchPerformance> performances = new ArrayList<>();
        for (String matchId : matchIds) {
            performances.add(riot.getMatch(matchId, puuid));
        }

        List<Sample> samples = new ArrayList<>();
        List<StatSignal> latestSignals = new ArrayList<>(); // segnali della partita più recente (i == 0)
        for (int i = 0; i < performances.size(); i++) {
            MatchPerformance performance = performances.get(i);
            MatchPerformance olderMatch = (i + 1 < performances.size()) ? performances.get(i + 1) : null;
            MatchAnalysisContext context = BreakDetector.detect(performance, olderMatch);

            String contextNote = context.isReturningAfterBreak()
                ? "Partita dopo lunga pausa. Possibile ruggine su timing, mani, automatismi."
                : null;

            List<StatSignal> signals = analyzer.analyze(performance);
            if (i == 0) latestSignals = signals;
            Observation obs = Observation.from(signals, performance.getMatchId(), ObservationType.GIOCO);
            memory.save(obs);
            samples.add(perceptor.observe(performance));
            String memPath = memoryWriter.write(MatchMemory.skeleton(
                performance.getMatchId(), performance.getChampion(), performance.getRole(),
                performance.isWin(), contextNote));
            String contextTag = context.isReturningAfterBreak() ? " | " + context : "";
            System.out.printf("  %s → score %.2f (%s)%s | memory → %s%n",
                performance.getMatchId(), obs.getScore(), performance.isWin() ? "WIN" : "LOSS",
                contextTag, memPath);
            if (contextNote != null) {
                System.out.println("    Nota: " + contextNote);
            }
        }

        System.out.println("\n--- learning ---");
        // Atlas continua: carica la memoria (nomi dal perceptor LoL), allena, risalva.
        // Il Trainer vede solo numeri; il battesimo dei pesi avviene qui, al confine.
        String[] featureNames = LoLPerceptor.FEATURE_NAMES;
        service.LearningStateService learningStore = new service.LearningStateService();
        model.learning.LearningState priorMemory = learningStore.load(featureNames);
        learning.PerceptronState trained =
            new Trainer(samples).run(20, learningStore.toPerceptronState(priorMemory));
        model.learning.LearningState updatedMemory =
            learningStore.elevate(trained, featureNames, priorMemory.totalEpochs() + 20);
        learningStore.save(updatedMemory);
        System.out.printf("Memoria    → %s (epoche totali: %d)%n",
            writer.LearningStateWriter.DEFAULT_PATH, updatedMemory.totalEpochs());

        System.out.println("\n--- summary ---");
        LearningLogSummary summary = new LearningLogReader().read("learning-log.txt");
        System.out.print(summary);

        RankFeatureVector features = new RankFeatureVectorBuilder().build(performances, "BOTTOM");
        RankPredictionResult rankResult = new RankPredictionEngine().predict(features);
        new RankPredictionView().show(rankResult);
        new RankPredictionHistoryWriter().append("memory/rank-prediction-history.csv", "BOTTOM", rankResult);
        new RankPredictionHistoryView().show("memory/rank-prediction-history.csv", 10);
        List<GroundTruthEntry> groundTruths = new GroundTruthReader().read("memory/rank-ground-truth.csv");
        new GroundTruthView().show(groundTruths);
        RankGap gap = null; // resta null se non c'è ground truth: Atlas non dichiara il gap che non conosce
        if (!groundTruths.isEmpty()) {
            Rank actual = Rank.parse(groundTruths.get(groundTruths.size() - 1).rank());
            gap = new RankGapAnalyzer().compute(rankResult.prediction().predictedRank(), actual);
            new RankGapView().show(gap);
            Rank goalTarget = new RankGoalReader().read("memory/rank-goal.csv");
            if (goalTarget != null) {
                RankGoalProgress goal = new RankGoalAnalyzer().compute(actual, goalTarget);
                RankGoalMatchEstimate estimate = new RankGoalMatchEstimator().estimate(goal);
                new RankGoalView().show(goal, estimate, rankResult.inputFeatures().winRate());
            }
        }

        model.coach.PlayerFocusProfile profile = new CoachProfileAnalyzer().analyze(performances);
        new CoachProfileView().show(profile);

        // Chiusura del cerchio: il motore pubblica lo stato consolidato come artefatto.
        // Da qui in poi la lente (GET /snapshot) legge questo file, non il motore vivo.
        if (!performances.isEmpty()) {
            model.insight.MatchInsight matchInsight = new analysis.insight.MatchInsightAnalyzer()
                    .analyze(latestSignals).orElse(null);
            model.snapshot.AtlasSnapshot snapshot = new service.SnapshotBuilder().build(
                    riotId,
                    performances.get(0).getMatchId(),
                    latestSignals,
                    matchInsight,
                    profile,
                    rankResult,
                    gap);
            writer.SnapshotWriter snapshotWriter = new writer.SnapshotWriter();
            model.snapshot.RiotAccountKey accountKey = model.snapshot.RiotAccountKey.fromRiotId(riotId);
            snapshotWriter.write(snapshot, writer.SnapshotWriter.DEFAULT_PATH);   // default / compatibilità
            snapshotWriter.writeForAccount(accountKey, snapshot);                 // confine per account
            System.out.println("\nSnapshot   → " + writer.SnapshotWriter.DEFAULT_PATH);
            System.out.println("Snapshot   → " + writer.SnapshotWriter.accountPath(accountKey));
        }
    }

    private static String buildLastGameText(
            MatchPerformance match,
            MatchAnalysisContext context,
            double score,
            String memPath) {
        int totalMin = (int) match.getMinutes();
        int secs = (int) Math.round((match.getMinutes() - totalMin) * 60);
        double csPerMin     = match.getCs() / match.getMinutes();
        double visionPerMin = match.getVisionScore() / match.getMinutes();
        String dateTime = Instant.ofEpochMilli(match.getGameStartTimestamp())
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════════\n");
        sb.append("ULTIMA PARTITA\n");
        sb.append("════════════════════════════════════\n");
        sb.append(String.format("Data/Ora : %s%n", dateTime));
        sb.append(String.format("Champion : %s%n", match.getChampion()));
        sb.append(String.format("Ruolo    : %s%n", match.getRole()));
        sb.append(String.format("Risultato: %s%n", match.isWin() ? "WIN" : "LOSS"));
        sb.append(String.format("Durata   : %d:%02d%n", totalMin, secs));
        sb.append("\n");
        sb.append(String.format("Score    : %.2f%n", score));
        sb.append("\n");
        sb.append(String.format("KDA      : %d / %d / %d%n", match.getKills(), match.getDeaths(), match.getAssists()));
        sb.append(String.format("CS/min   : %.2f%n", csPerMin));
        sb.append(String.format("Vision   : %.2f%n", visionPerMin));
        sb.append("\n");
        if (context.isReturningAfterBreak()) {
            sb.append(String.format("Contesto : RETURNING_AFTER_BREAK +%dh%n", context.getHoursSinceLastGame()));
            sb.append("Nota     : Partita dopo lunga pausa. Possibile ruggine su timing, mani, automatismi.\n");
        } else {
            sb.append("Contesto : NORMAL\n");
        }
        sb.append("\n");
        sb.append(String.format("Memory   : %s%n", memPath));
        sb.append("════════════════════════════════════\n");
        return sb.toString();
    }

    /**
     * Slice pre-coda: legge da console lo stato dichiarato, lo mostra e lo salva
     * come fascicolo .txt. Non analizza — raccoglie.
     * Il Main decide dove: prequeue/&lt;data&gt;_&lt;ora&gt;.txt.
     */
    private static void runPreQueue() throws IOException {
        PreQueueSnapshot snapshot = new PreQueueConsoleReader().read();
        new PreQueueSnapshotView().show(snapshot);

        // Il Main decide dove: stesso nome, due viste dello stesso evento.
        // .txt lo leggi tu, .json lo rilegge Atlas.
        String base = snapshot.createdAt()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        PreQueueSnapshotWriter preQueueWriter = new PreQueueSnapshotWriter();
        Path text = preQueueWriter.writeText(snapshot, Path.of("prequeue", base + ".txt"));
        Path json = preQueueWriter.writeJson(snapshot, Path.of("prequeue", base + ".json"));
        System.out.println("Diario   : " + text);
        System.out.println("Fascicolo: " + json);
    }

    private static void saveLastGameReport(String text) throws IOException {
        Path dir = Path.of("reports");
        Files.createDirectories(dir);
        Path file = dir.resolve("last-game.txt");
        Files.writeString(file, text, StandardCharsets.UTF_8);
        System.out.println("Salvato  : " + file);
    }
}
