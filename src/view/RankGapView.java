package view;

import model.rank.RankGap;

public class RankGapView {

    public void show(RankGap gap) {
        String predicted = rankLabel(gap.predicted());
        String actual    = rankLabel(gap.actual());
        int g = gap.gapInDivisions();

        System.out.println("=== Rank Gap ===");
        System.out.printf("Previsto : %s%n", predicted);
        System.out.printf("Reale    : %s%n", actual);
        if (g == 0) {
            System.out.println("Gap      : 0 divisioni (rank allineato alle performance)");
        } else if (g > 0) {
            System.out.printf("Gap      : +%d divisioni (giochi sopra il tuo rank)%n", g);
        } else {
            System.out.printf("Gap      : %d divisioni (giochi sotto il tuo rank)%n", g);
        }
        System.out.println("================");
    }

    private String rankLabel(model.rank.Rank rank) {
        if (rank.division() == model.rank.RankDivision.NONE) return rank.tier().name();
        return rank.tier().name() + " " + rank.division().name();
    }
}
