package model.queue;

/**
 * Freno dichiarato prima della coda.
 * Atlas registra ciò che il giocatore percepisce come ostacolo, non ciò che è "vero".
 */
public enum QueueBlocker {
    LOSING_BECAUSE_OF_OTHERS,
    TRYING_TOO_HARD,
    TIRED,
    NONE,
    OTHER
}
