package model.queue;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Stato mentale dichiarato prima di entrare in coda.
 * È una fotografia, non una diagnosi.
 */
public record PreQueueSnapshot(
        LocalDateTime createdAt,
        RankThought rankThought,
        List<QueueBlocker> blockers,
        GameWindow window,
        String reminder
) {
    public static final String DEFAULT_REMINDER =
            "NON HAI RANK. Gioca un game alla volta.";
}
