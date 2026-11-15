
package it.workflow.predictor.reliability;

import java.math.BigDecimal;
import java.util.Objects;

import org.oristool.eulero.modeling.Simple;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

public final class ExponentialRetryTask implements RetryTask {

    private final String name;
    private final BigDecimal lambda;
    private final RetryConfig retry;

    public ExponentialRetryTask(
            String name,
            BigDecimal lambda,
            RetryConfig retry) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome del task obbligatorio");
        }

        Objects.requireNonNull(lambda);
        Objects.requireNonNull(retry);

        if (lambda.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Lambda deve essere positivo");
        }

        this.name = name;
        this.lambda = lambda;
        this.retry = retry;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public RetryConfig retry() {
        return retry;
    }

    // F(t) = 1 - exp(-lambda * t)
    @Override
    public double cdf(double time) {

        if (time <= 0) return 0.0;

        double rate = lambda.doubleValue();

        return -Math.expm1(-rate * time);
    }

    // Distribuzione originale dell'esponenziale.
    @Override
    public Simple originalActivity(String suffix) {

        return Simple.exp(name + suffix, lambda);
    }

    // Distribuzione esponenziale condizionata
    // al completamento entro il timeout.
    @Override
    public Simple successfulAttempt(String activityName) {

        BigDecimal timeout = retry.timeout();

        double rate = lambda.doubleValue();
        double t = timeout.doubleValue();

        double successProbability =
                -Math.expm1(-rate * t);

        if (successProbability <= 0.0) {
            throw new IllegalArgumentException(
                    "Probabilita' di successo nulla");
        }

        // Densita' condizionata:
        // lambda * exp(-lambda*x) / (1-exp(-lambda*t))
        double coefficient = rate / successProbability;

        String density =
                BigDecimal.valueOf(coefficient).toPlainString()
                        + "*Exp[-"
                        + lambda.toPlainString()
                        + " x]";

        StochasticTransitionFeature feature =
                StochasticTransitionFeature.newExpolynomial(
                        density,
                        OmegaBigDecimal.ZERO,
                        new OmegaBigDecimal(timeout)
                );

        return new Simple(activityName, feature);
    }
}
