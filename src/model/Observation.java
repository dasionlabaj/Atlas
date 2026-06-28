package model;

import java.util.List;

public class Observation {

    private String source;
    private String matchId;
    private ObservationType type;
    private double score;
    private long timestamp;

    private Observation(String source, String matchId, ObservationType type, double score, long timestamp) {
        this.source = source;
        this.matchId = matchId;
        this.type = type;
        this.score = score;
        this.timestamp = timestamp;
    }

    public static Observation fromPersisted(long timestamp, String source, String matchId, ObservationType type, double score) {
        return new Observation(source, matchId, type, score, timestamp);
    }

    public static Observation from(List<StatSignal> signals, String matchId, ObservationType type) {
        double score = computeScore(signals);
        return new Observation("LOL_MATCH", matchId, type, score, System.currentTimeMillis());
    }

    private static double computeScore(List<StatSignal> signals) {
        if (signals.isEmpty()) return 0.0;
        double total = 0.0;
        for (StatSignal signal : signals) {
            if (signal.getColor() == SignalColor.GREEN) total += 1.0;
            else if (signal.getColor() == SignalColor.YELLOW) total += 0.5;
        }
        return total / signals.size();
    }

    public String getSource() {
        return source;
    }

    public String getMatchId() {
        return matchId;
    }

    public ObservationType getType() {
        return type;
    }

    public double getScore() {
        return score;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
