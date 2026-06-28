package analysis.rank;

import java.util.List;
import java.util.stream.Collectors;
import model.MatchPerformance;

public class RankFeatureVectorBuilder {

    public RankFeatureVector build(List<MatchPerformance> matches) {
        return build(matches, null);
    }

    public RankFeatureVector build(List<MatchPerformance> matches, String role) {
        if (matches == null || matches.isEmpty()) {
            return new RankFeatureVector(0.0, 0.0, 0.0, 0.0, 0.0, 0);
        }

        List<MatchPerformance> filtered = (role == null || role.isBlank())
            ? matches
            : matches.stream()
                .filter(m -> role.equalsIgnoreCase(m.getRole()))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return new RankFeatureVector(0.0, 0.0, 0.0, 0.0, 0.0, 0);
        }

        int sampleSize = filtered.size();
        int wins = 0;
        double totalKda = 0.0;
        double totalVisionScore = 0.0;
        double totalDamagePerMinute = 0.0;
        double totalCsPerMinute = 0.0;

        for (MatchPerformance m : filtered) {
            if (m.isWin()) wins++;

            double deaths = Math.max(m.getDeaths(), 1.0);
            totalKda += (m.getKills() + m.getAssists()) / deaths;

            double minutes = m.getMinutes() > 0.0 ? m.getMinutes() : 1.0;
            totalVisionScore      += m.getVisionScore();
            totalDamagePerMinute  += m.getDamageDealt() / minutes;
            totalCsPerMinute      += m.getCs()          / minutes;
        }

        double n = sampleSize;
        return new RankFeatureVector(
            wins / n,
            totalKda             / n,
            totalVisionScore     / n,
            totalDamagePerMinute / n,
            totalCsPerMinute     / n,
            sampleSize
        );
    }
}
