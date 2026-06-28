package model.queue;

/**
 * Ciò che apre la finestra di gioco.
 * È il contesto esterno che rende naturale entrare in coda, non una motivazione.
 */
public enum GameWindowTrigger {
    CLAUDE_COUNTER,
    FREE_TIME,
    AFTER_STUDY,
    BEFORE_COMMITMENT,
    OTHER
}
