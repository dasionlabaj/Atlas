package model;

/**
 * Un semaforo: l'osservazione di un singolo numero, primo livello della
 * gerarchia di conoscenza di Atlas. Descrive, non spiega. Mettere in relazione
 * più segnali è un altro livello (MatchInsight/Coach), non vive qui.
 *
 * value          — il numero grezzo osservato
 * color          — dove cade rispetto alle soglie (GREEN/YELLOW/RED)
 * green/yellow   — le soglie contro cui è stato letto, perché il cassetto
 *                  (il dettaglio espandibile) le possa mostrare senza riderivare
 * reason         — l'osservazione in lingua umana del numero, non un consiglio
 */
public class StatSignal {

    private String name;
    private SignalColor color;
    private double value;
    private double greenThreshold;
    private double yellowThreshold;
    private String reason;

    public StatSignal(String name, SignalColor color, double value,
                      double greenThreshold, double yellowThreshold, String reason) {
        this.name = name;
        this.color = color;
        this.value = value;
        this.greenThreshold = greenThreshold;
        this.yellowThreshold = yellowThreshold;
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public SignalColor getColor() {
        return color;
    }

    public double getValue() {
        return value;
    }

    public double getGreenThreshold() {
        return greenThreshold;
    }

    public double getYellowThreshold() {
        return yellowThreshold;
    }

    public String getReason() {
        return reason;
    }
}
