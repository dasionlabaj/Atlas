package analysis.coach;

import java.util.ArrayList;
import java.util.List;

import model.Evidence;
import model.coach.CoachHypothesis;
import model.coach.ObservationGauge;
import model.coach.SessionBattery;

/**
 * Trasforma le osservazioni (gauge) in ipotesi.
 * Ogni ipotesi è una possibile correlazione, non un comando.
 * La confidenza non è inventata: viene dalla confidenza del gauge da cui nasce.
 */
public class CoachHypothesisGenerator {

    public List<CoachHypothesis> generate(ObservationGauge cs,
                                          ObservationGauge vision,
                                          ObservationGauge deaths,
                                          ObservationGauge kda,
                                          SessionBattery battery,
                                          int sampleSize) {
        List<CoachHypothesis> out = new ArrayList<>();

        if ("stabile".equals(cs.trend()) && cs.confidence() >= 50) {
            out.add(new CoachHypothesis(
                "Negli ultimi " + sampleSize + " game il CS è stabile.",
                "La crescita del rank potrebbe non essere limitata dal farming.",
                cs.confidence(),
                List.of(new Evidence(cs.name(), "stabile"))));
        }

        if ("in calo".equals(vision.trend())) {
            out.add(new CoachHypothesis(
                "La vision cala progressivamente nelle partite più recenti.",
                "La raccolta di informazioni potrebbe ridursi quando la sessione si allunga.",
                vision.confidence(),
                List.of(new Evidence(vision.name(), "in calo"))));
        }

        if ("in calo".equals(deaths.trend())) { // gauge deaths in calo = più morti
            out.add(new CoachHypothesis(
                "Le morti aumentano nelle partite più recenti.",
                "Il rischio assunto potrebbe crescere più del vantaggio che produce.",
                deaths.confidence(),
                // gauge "in calo" = morti in aumento: la frase parla al giocatore, non al sensore
                List.of(new Evidence(deaths.name(), "in aumento"))));
        }

        if ("in miglioramento".equals(kda.trend())) {
            out.add(new CoachHypothesis(
                "Il KDA è in miglioramento nelle partite recenti.",
                "Le scelte in combattimento potrebbero essere diventate più selettive.",
                kda.confidence(),
                List.of(new Evidence(kda.name(), "in miglioramento"))));
        }

        if (battery.currentEnergy() < 40) {
            out.add(new CoachHypothesis(
                "La consistenza cala all'interno della sessione corrente.",
                "La capacità cognitiva residua potrebbe essere in diminuzione.",
                // confidenza ancorata all'ampiezza del campione osservato, non scelta a mano
                Math.min(100.0, sampleSize * 8.0),
                List.of(new Evidence("Sessione", "consistenza in calo"))));
        }

        if (out.isEmpty()) {
            out.add(new CoachHypothesis(
                "Le dimensioni osservate sono stabili o in crescita.",
                "Il sistema non rileva pattern di deriva nelle partite recenti.",
                average(cs.confidence(), vision.confidence(), deaths.confidence(), kda.confidence()),
                List.of(
                    new Evidence(cs.name(), cs.trend()),
                    new Evidence(vision.name(), vision.trend()),
                    new Evidence(deaths.name(), deaths.trend()),
                    new Evidence(kda.name(), kda.trend()))));
        }

        return out;
    }

    private double average(double... xs) {
        double s = 0.0;
        for (double x : xs) s += x;
        return xs.length == 0 ? 0.0 : s / xs.length;
    }
}
