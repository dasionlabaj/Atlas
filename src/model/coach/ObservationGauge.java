package model.coach;

/**
 * Una singola osservazione di Atlas su una dimensione del gioco.
 * value       = quanto Atlas ha osservato (0-100), non un giudizio sul giocatore
 * confidence  = quanto Atlas si fida di questa osservazione (0-100)
 * trend       = direzione recente: "in miglioramento" | "stabile" | "in calo"
 */
public record ObservationGauge(
        String name,
        double value,
        double confidence,
        String trend
) {}
