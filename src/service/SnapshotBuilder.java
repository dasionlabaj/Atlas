package service;

import java.time.Instant;
import java.util.List;

import model.StatSignal;
import model.coach.ObservationGauge;
import model.coach.PlayerFocusProfile;
import model.coach.SessionBattery;
import model.insight.MatchInsight;
import model.rank.Rank;
import model.rank.RankDivision;
import model.rank.RankGap;
import model.snapshot.AtlasSnapshot;
import model.snapshot.SnapshotFocusArea;
import model.snapshot.SnapshotRankPrediction;
import model.snapshot.SnapshotSessionBattery;
import model.snapshot.SnapshotSignal;
import model.snapshot.SnapshotStatus;
import model.snapshot.SnapshotSummary;
import analysis.rank.RankPredictionResult;

/**
 * Assembla lo stato consolidato a partire da ciò che il motore ha già prodotto.
 *
 *   StatSignal + PlayerFocusProfile + RankPredictionResult (+ RankGap)
 *           ↓  riduzione a linguaggio pubblico
 *      AtlasSnapshot
 *
 * Regola: Dominio = ciò che Atlas usa per pensare. Snapshot = ciò che Atlas sceglie
 * di dire. Qui non si analizza nulla: i record grezzi del dominio diventano viste
 * dichiarative piccole e stabili. Atlas non dichiara ciò che non sa (campi null
 * spariscono dal JSON).
 *
 * Nessuna chiamata al motore qui dentro: riceve i pezzi pronti, così Main non si gonfia.
 */
public class SnapshotBuilder {

    public AtlasSnapshot build(String summoner,
                               String lastMatchId,
                               List<StatSignal> latestSignals,
                               MatchInsight matchInsight,
                               PlayerFocusProfile profile,
                               RankPredictionResult rankResult,
                               RankGap rankGap) {

        SnapshotSummary summary = new SnapshotSummary(
                summoner,
                SnapshotStatus.OBSERVING,
                lastMatchId);

        List<SnapshotSignal> signals = latestSignals.stream()
                .map(s -> new SnapshotSignal(s.getName(), s.getColor(), s.getValue(),
                        s.getReason(), s.getGreenThreshold(), s.getYellowThreshold()))
                .toList();

        return new AtlasSnapshot(
                summary,
                signals,
                matchInsight,
                List.copyOf(profile.hypotheses()),
                rankView(rankResult, rankGap),
                focusView(profile),
                batteryView(profile.battery()),
                Instant.now().toString(),          // quando Atlas ha osservato
                AtlasSnapshot.CONTRACT_VERSION);
    }

    private SnapshotRankPrediction rankView(RankPredictionResult rankResult, RankGap gap) {
        if (rankResult == null) return null;
        boolean hasTruth = gap != null;
        return new SnapshotRankPrediction(
                formatRank(rankResult.prediction().predictedRank()),
                rankResult.prediction().confidence() * 100.0, // dominio 0-1 → contratto 0-100, scala unica
                hasTruth ? formatRank(gap.actual()) : null,
                hasTruth ? formatGap(gap.gapInDivisions()) : null);
    }

    private List<SnapshotFocusArea> focusView(PlayerFocusProfile profile) {
        return List.of(
                area(profile.cs()),
                area(profile.vision()),
                area(profile.deaths()),
                area(profile.kda()));
    }

    private SnapshotFocusArea area(ObservationGauge g) {
        return new SnapshotFocusArea(g.name(), g.value(), confidenceLabel(g.confidence()), g.trend());
    }

    private SnapshotSessionBattery batteryView(SessionBattery battery) {
        return new SnapshotSessionBattery(battery.currentEnergy(), battery.status());
    }

    /** "GOLD_I", o solo il tier se non c'è divisione. Linguaggio pubblico, stabile. */
    private String formatRank(Rank rank) {
        if (rank.division() == RankDivision.NONE) return rank.tier().name();
        return rank.tier().name() + "_" + rank.division().name();
    }

    private String formatGap(int divisions) {
        if (divisions == 0) return "0 divisioni";
        return (divisions > 0 ? "+" + divisions : String.valueOf(divisions)) + " divisioni";
    }

    /** Stesse soglie della vista console: la confidenza numerica diventa etichetta. */
    private String confidenceLabel(double confidence) {
        if (confidence >= 66) return "ALTA";
        if (confidence >= 33) return "MEDIA";
        return "BASSA";
    }
}
