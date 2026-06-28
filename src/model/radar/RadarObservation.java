package model.radar;

/**
 * Il Radar non produce giudizi.
 * Produce osservazioni sullo stato corrente.
 *
 * Ogni misura è relativa alle osservazioni disponibili
 * e può cambiare quando arrivano nuovi dati.
 */
public interface RadarObservation {

    String name();

    double confidence();

}
