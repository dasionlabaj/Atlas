package model.coach;

import java.util.List;

public record PlayerFocusProfile(
        ObservationGauge cs,
        ObservationGauge vision,
        ObservationGauge deaths,
        ObservationGauge kda,
        SessionBattery battery,
        List<CoachHypothesis> hypotheses,
        CoachReflection reflection
) {}
