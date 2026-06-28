package service;

import java.time.Instant;
import java.util.List;

import analysis.MatchAnalysisContext;
import model.MatchPerformance;
import model.StatSignal;
import model.export.AtlasGameExport;
import model.export.AtlasGameExport.ExportContext;
import model.export.AtlasGameExport.ExportMatch;
import model.export.AtlasGameExport.ExportSignal;

/**
 * Proietta ciò che il motore ha già prodotto (LastGameAnalysis) nel contratto di
 * esportazione AtlasGameExport. Gemello di {@link SnapshotBuilder}, ma su un'altra
 * lente:
 *
 *   LastGameAnalysis ──► AtlasSnapshot      (consolidato → UI/REST)   SnapshotBuilder
 *   LastGameAnalysis ──► AtlasGameExport    (ultima partita → chat)   questo
 *
 * Regola identica a SnapshotBuilder: qui non si analizza nulla. I fatti grezzi e i
 * giudizi che Atlas ha già calcolato diventano una vista dichiarativa, stabile,
 * incollabile altrove. Pura: nessun IO, nessun effetto.
 */
public class AtlasGameExportBuilder {

    public AtlasGameExport build(String summoner, LastGameAnalysis analysis) {
        MatchPerformance match = analysis.getCurrent();
        MatchAnalysisContext context = analysis.getContext();

        ExportMatch exportMatch = new ExportMatch(
                match.getMatchId(),
                Instant.ofEpochMilli(match.getGameStartTimestamp()).toString(),
                match.getChampion(),
                match.getRole(),
                match.isWin() ? "WIN" : "LOSS",
                match.getMinutes(),
                match.getKills(),
                match.getDeaths(),
                match.getAssists());

        ExportContext exportContext = context.isReturningAfterBreak()
                ? new ExportContext("RETURNING_AFTER_BREAK", context.getHoursSinceLastGame())
                : new ExportContext("NORMAL", null);

        List<ExportSignal> signals = analysis.getSignals().stream()
                .map(this::toExportSignal)
                .toList();

        return new AtlasGameExport(
                summoner,
                exportMatch,
                exportContext,
                analysis.getScore(),
                signals,
                Instant.now().toString(),          // quando Atlas ha esportato
                AtlasGameExport.CONTRACT_VERSION);
    }

    private ExportSignal toExportSignal(StatSignal s) {
        return new ExportSignal(
                s.getName(),
                s.getColor().name(),
                s.getValue(),
                s.getReason(),
                s.getGreenThreshold(),
                s.getYellowThreshold());
    }
}
