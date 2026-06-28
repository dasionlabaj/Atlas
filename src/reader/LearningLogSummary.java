package reader;

import java.util.Locale;

public class LearningLogSummary {

    public final int totalTrained;
    public final int totalSkipped;
    public final int correct;
    public final int errors;
    public final double accuracy;
    public final double[] weightFirst;
    public final double[] weightLast;

    LearningLogSummary(int totalTrained, int totalSkipped, int correct,
                       double[] weightFirst, double[] weightLast) {
        this.totalTrained = totalTrained;
        this.totalSkipped = totalSkipped;
        this.correct      = correct;
        this.errors       = totalTrained - correct;
        this.accuracy     = totalTrained > 0 ? (double) correct / totalTrained : 0.0;
        this.weightFirst  = weightFirst;
        this.weightLast   = weightLast;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  trained : %d  |  skipped : %d%n", totalTrained, totalSkipped));
        sb.append(String.format("  correct : %d  |  errors  : %d  |  accuracy : %.1f%%%n",
            correct, errors, accuracy * 100));
        if (weightFirst != null && weightLast != null) {
            sb.append(String.format("  weight trend (prima -> dopo):%n"));
            for (int i = 0; i < weightFirst.length; i++) {
                double delta = weightLast[i] - weightFirst[i];
                sb.append(String.format(Locale.US, "    f%-9d  %+.4f -> %+.4f  (d %+.4f)%n",
                    i, weightFirst[i], weightLast[i], delta));
            }
        }
        return sb.toString();
    }
}
