package reader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import model.queue.GameWindow;
import model.queue.GameWindowTrigger;
import model.queue.PreQueueSnapshot;
import model.queue.QueueBlocker;
import model.queue.RankThought;

/**
 * Legge da console lo stato pre-coda e lo trasforma in PreQueueSnapshot.
 * Raccoglie, non interpreta: nessuna analisi, solo ciò che il giocatore dichiara.
 */
public class PreQueueConsoleReader {

    private final Scanner in;

    public PreQueueConsoleReader() {
        this(new Scanner(System.in));
    }

    public PreQueueConsoleReader(Scanner in) {
        this.in = in;
    }

    public PreQueueSnapshot read() {
        LocalDateTime now = LocalDateTime.now();

        RankThought rankThought = askEnum(
                "Pensiero sul rank", RankThought.values(), RankThought.NEUTRAL);
        List<QueueBlocker> blockers = askBlockers();
        int minutes = askInt("Durata finestra (minuti)", 180);
        GameWindowTrigger trigger = askEnum(
                "Trigger della finestra", GameWindowTrigger.values(), GameWindowTrigger.FREE_TIME);
        String windowNote = askLine("Nota sulla finestra (invio per saltare)");

        GameWindow window = new GameWindow(now, now.plusMinutes(minutes), trigger, windowNote);

        String reminder = askLine("Promemoria (invio per default)");
        if (reminder.isBlank()) {
            reminder = PreQueueSnapshot.DEFAULT_REMINDER;
        }

        return new PreQueueSnapshot(now, rankThought, blockers, window, reminder);
    }

    private <E extends Enum<E>> E askEnum(String label, E[] options, E fallback) {
        System.out.println();
        System.out.println(label + ":");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d) %s%n", i + 1, options[i]);
        }
        System.out.print("> ");
        String raw = line();
        try {
            int choice = Integer.parseInt(raw);
            if (choice >= 1 && choice <= options.length) {
                return options[choice - 1];
            }
        } catch (NumberFormatException ignored) {
            // input non numerico → fallback
        }
        System.out.println("  (scelta non valida, uso " + fallback + ")");
        return fallback;
    }

    private List<QueueBlocker> askBlockers() {
        QueueBlocker[] options = QueueBlocker.values();
        System.out.println();
        System.out.println("Freni (indici separati da virgola, invio per nessuno):");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d) %s%n", i + 1, options[i]);
        }
        System.out.print("> ");
        String raw = line();
        List<QueueBlocker> selected = new ArrayList<>();
        if (raw.isBlank()) {
            return selected;
        }
        for (String token : raw.split(",")) {
            try {
                int choice = Integer.parseInt(token.trim());
                if (choice >= 1 && choice <= options.length) {
                    QueueBlocker blocker = options[choice - 1];
                    if (!selected.contains(blocker)) {
                        selected.add(blocker);
                    }
                }
            } catch (NumberFormatException ignored) {
                // token non numerico → ignorato
            }
        }
        return selected;
    }

    private int askInt(String label, int fallback) {
        System.out.println();
        System.out.print(label + " [" + fallback + "]: ");
        String raw = line();
        if (raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            System.out.println("  (non numerico, uso " + fallback + ")");
            return fallback;
        }
    }

    private String askLine(String label) {
        System.out.println();
        System.out.print(label + ": ");
        return line();
    }

    /** Legge una riga, scarta un eventuale BOM iniziale (input rediretto) e ripulisce. */
    private String line() {
        String raw = in.nextLine();
        if (!raw.isEmpty() && raw.charAt(0) == '\uFEFF') {
            raw = raw.substring(1);
        }
        return raw.trim();
    }
}
