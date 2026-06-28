package view;

import model.rank.Rank;
import model.rank.RankDivision;
import model.rank.RankGoalMatchEstimate;
import model.rank.RankGoalProgress;

public class RankGoalView {

    public void show(RankGoalProgress progress, RankGoalMatchEstimate estimate, double winRate) {
        System.out.println("=== Rank Goal ===");
        System.out.printf("Attuale   : %s%n", rankLabel(progress.currentRank()));
        System.out.printf("Obiettivo : %s%n", rankLabel(progress.targetRank()));
        System.out.printf("Mancano   : %d divisioni%n", progress.remainingDivisions());
        System.out.printf("Stima     : ~%d partite positive%n", estimate.pseudoGamesRemaining());
        System.out.printf("Ritmo     : %.0f%% winrate%n", winRate * 100);
        System.out.printf("Progresso : %.0f%%%n", progress.completionPercentage());
        System.out.println("=================");
    }

    private String rankLabel(Rank rank) {
        if (rank.division() == RankDivision.NONE) return rank.tier().name();
        return rank.tier().name() + " " + rank.division().name();
    }
}
