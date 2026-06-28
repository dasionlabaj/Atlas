package model.radar;

/**
 * Una misura Radar non rappresenta una verità.
 * Rappresenta una posizione osservata.
 */
public record RadarSignal(
        String axis,
        double value,
        double confidence
) implements RadarObservation {

    @Override
    public String name() {
        return axis;
    }
}
