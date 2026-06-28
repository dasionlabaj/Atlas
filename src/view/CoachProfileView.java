package view;

import model.Evidence;
import model.coach.CoachHypothesis;
import model.coach.ObservationGauge;
import model.coach.PlayerFocusProfile;

public class CoachProfileView {

    public void show(PlayerFocusProfile profile) {
        System.out.println("=== Coach Profile ===");
        row(profile.cs());
        row(profile.vision());
        row(profile.deaths());
        row(profile.kda());
        System.out.println();
        System.out.printf("Batteria sessione: %d%% — %s%n",
                profile.battery().currentEnergy(), profile.battery().status());

        System.out.println();
        System.out.println("--- Ipotesi ---");
        for (CoachHypothesis h : profile.hypotheses()) {
            System.out.println();
            System.out.println("OSSERVAZIONE");
            System.out.println(h.observation());
            System.out.println("IPOTESI");
            System.out.println(h.hypothesis());
            System.out.println("CONFIDENZA");
            System.out.printf("%.0f%%%n", h.confidence());
            if (!h.evidence().isEmpty()) {
                System.out.println("EVIDENZE");
                for (Evidence e : h.evidence()) {
                    System.out.println("  " + e.source() + " · " + e.description());
                }
            }
        }

        System.out.println();
        System.out.println("Domanda Atlas:");
        System.out.println(profile.reflection().question());
        System.out.println("=====================");
    }

    private void row(ObservationGauge g) {
        System.out.printf("%-8s : %3.0f%% | Confidenza: %-5s | Trend: %s%n",
                g.name(), g.value(), confidenceLabel(g.confidence()), g.trend());
    }

    private String confidenceLabel(double confidence) {
        if (confidence >= 66) return "ALTA";
        if (confidence >= 33) return "MEDIA";
        return "BASSA";
    }
}
