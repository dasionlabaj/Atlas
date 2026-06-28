package perceptor.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import perceptor.Sample;

public class TestEncoder {

    private final long seed;

    public TestEncoder(long seed) {
        this.seed = seed;
    }

    public List<Sample> generate(int count) {
        Random rng = new Random(seed);
        List<Sample> samples = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double a = rng.nextDouble();
            double b = rng.nextDouble();
            double[] features = { a, b };
            int expected = (a + b > 1.0) ? 1 : 0;
            samples.add(new Sample(features, expected, true));
        }
        return samples;
    }
}
