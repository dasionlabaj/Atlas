package model.learning;

/**
 * Un peso con un nome.
 *
 * Finché i pesi sono un double[] anonimo, "f2" non significa nulla. Qui f2 diventa
 * "Vision/min": il peso smette di essere un numero e diventa memoria con identità.
 *
 * Il nome è semantica di dominio (LoL): NON entra mai nel Trainer, che resta
 * domain-blind. È il confine domain-aware a battezzare i numeri.
 */
public record FeatureWeight(
        String name,
        double value
) {}
