package policy;

/**
 * Posizione di un valore rispetto alle soglie — la SEMANTICA, non il colore.
 * Token stabile e verificabile: il Coach, quando nascerà, leggerà questo,
 * non la stringa umana del reason.
 */
public enum ThresholdSemantic {
    AT_OR_ABOVE_GREEN,
    BELOW_GREEN,
    BELOW_YELLOW
}
