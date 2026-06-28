package perceptor;

public class Sample {
    public final double[] features;
    public final int expected;
    public final boolean trainable;

    public Sample(double[] features, int expected, boolean trainable) {
        this.features = features;
        this.expected = expected;
        this.trainable = trainable;
    }
}
