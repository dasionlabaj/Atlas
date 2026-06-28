package analysis.coach;

import model.coach.CoachReflection;
import model.coach.ObservationGauge;
import model.coach.SessionBattery;

/**
 * Genera una domanda a partire dalle osservazioni. Mai un imperativo.
 * Pattern: "Ho osservato Y. Secondo te <domanda>?"
 * Il giocatore resta al centro: Atlas non conclude, apre.
 */
public class CoachReflectionGenerator {

    public CoachReflection generate(ObservationGauge cs,
                                    ObservationGauge vision,
                                    ObservationGauge deaths,
                                    ObservationGauge kda,
                                    SessionBattery battery) {

        // Caso firma: vision che cala mentre le morti aumentano (deaths gauge in calo)
        if ("in calo".equals(vision.trend()) && "in calo".equals(deaths.trend())) {
            return new CoachReflection(
                "Nelle ultime partite la vision cala mentre le morti aumentano. "
              + "Secondo te stai giocando senza informazioni, o le hai e forzi comunque?");
        }

        // Sessione scarica: nessun comando, solo lo specchio
        if (battery.currentEnergy() < 40) {
            return new CoachReflection(
                "Ho contato diverse partite ravvicinate e l'energia stimata della sessione è bassa. "
              + "Secondo te le ultime scelte sono state lucide come le prime?");
        }

        // Altrimenti: la dimensione che peggiora di più
        ObservationGauge declining = mostDeclining(cs, vision, deaths, kda);
        if (declining != null) {
            return new CoachReflection(
                "Ho osservato " + declining.name() + " in calo nelle partite recenti. "
              + "Secondo te è cambiato qualcosa nel modo in cui giochi, o è solo varianza?");
        }

        // Tutto stabile o in crescita: domanda che consolida, non elogio
        return new CoachReflection(
            "Le dimensioni che osservo sono stabili o in crescita. "
          + "Secondo te cosa stai facendo di diverso rispetto a quando non lo erano?");
    }

    private ObservationGauge mostDeclining(ObservationGauge... gauges) {
        ObservationGauge worst = null;
        for (ObservationGauge g : gauges) {
            if (!"in calo".equals(g.trend())) continue;
            if (worst == null || g.value() < worst.value()) worst = g;
        }
        return worst;
    }
}
