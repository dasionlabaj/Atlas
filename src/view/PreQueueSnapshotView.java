package view;

import model.queue.PreQueueSnapshot;

public class PreQueueSnapshotView {

    public void show(PreQueueSnapshot snapshot) {
        System.out.println();
        System.out.println("=== PRE QUEUE ===");
        System.out.println(snapshot.reminder());
        System.out.println();

        System.out.println("Pensiero sul rank: " + snapshot.rankThought());
        System.out.println("Freni dichiarati: " + snapshot.blockers());

        if (snapshot.window() != null) {
            System.out.println();
            System.out.println("Finestra di gioco:");
            System.out.println("- Inizio: " + snapshot.window().start());
            System.out.println("- Fine prevista: " + snapshot.window().expectedEnd());
            System.out.println("- Trigger: " + snapshot.window().trigger());
            System.out.println("- Nota: " + snapshot.window().note());
        }

        System.out.println("=================");
        System.out.println();
    }
}
