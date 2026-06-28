package view;

import java.util.List;
import reader.GroundTruthEntry;

public class GroundTruthView {

    public void show(List<GroundTruthEntry> entries) {
        System.out.println("--- rank ground truth ---");
        if (entries.isEmpty()) {
            System.out.println("(nessun dato confermato)");
        } else {
            for (GroundTruthEntry e : entries) {
                System.out.printf("%s | %s | %s%n", e.date(), e.rank(), e.source());
            }
        }
        System.out.println("-------------------------");
    }
}
