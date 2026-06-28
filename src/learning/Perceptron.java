package learning;

public class Perceptron {

    private final PerceptronState state;

    public Perceptron(PerceptronState state) {
        this.state = state;
    }

    public int predict(double[] input) {
        double sum = state.bias;

        for (int i = 0; i < input.length; i++) {
            sum += input[i] * state.weights[i];
        }

        return sum >= 0 ? 1 : 0;
    }

    public int train(double[] input, int expected) {
        int prediction = predict(input);
        int error = expected - prediction;

        for (int i = 0; i < state.weights.length; i++) {
            state.weights[i] += state.learningRate * error * input[i];
        }

        state.bias += state.learningRate * error;

        return prediction;
    }

    public PerceptronState state() {
        return state;
    }
}
