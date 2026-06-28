package memory;

import java.util.List;

import model.Observation;

public class MemorySummary {

    private int observations;
    private double averageScore;
    private double lastScore;
    private double trend;

    private MemorySummary(int observations, double averageScore, double lastScore, double trend) {
        this.observations = observations;
        this.averageScore = averageScore;
        this.lastScore = lastScore;
        this.trend = trend;
    }

    public static MemorySummary from(List<Observation> observations) {
        if (observations.isEmpty()) {
            return new MemorySummary(0, 0.0, 0.0, 0.0);
        }

        int count = observations.size();
        double total = 0.0;
        for (Observation obs : observations) {
            total += obs.getScore();
        }
        double average = total / count;
        double last = observations.get(count - 1).getScore();
        double trend = last - average;

        return new MemorySummary(count, average, last, trend);
    }

    public int getObservations() {
        return observations;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public double getLastScore() {
        return lastScore;
    }

    public double getTrend() {
        return trend;
    }
}
