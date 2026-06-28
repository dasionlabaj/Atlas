package model.queue;

import java.time.LocalDateTime;

/**
 * Finestra temporale percepita per giocare.
 * Non decide cosa fare: registra il contesto che rende più facile entrare in coda.
 */
public record GameWindow(
        LocalDateTime start,
        LocalDateTime expectedEnd,
        GameWindowTrigger trigger,
        String note
) {
}
