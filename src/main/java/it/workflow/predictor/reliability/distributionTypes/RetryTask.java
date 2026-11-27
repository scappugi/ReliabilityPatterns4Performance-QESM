
package it.workflow.predictor.reliability.distributionTypes;

import it.workflow.predictor.reliability.RetryConfig;

import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;
import org.oristool.eulero.modeling.stochastictime.ExpolynomialTime;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;

import java.math.BigDecimal;
import java.util.Objects;

public final class RetryTask {

    private final String name;
    private final StochasticTime distribution;
    private final RetryConfig retry;

    public RetryTask(
            String name,
            StochasticTime distribution,
            RetryConfig retry) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome non valido");
        }

        this.name = name;
        this.distribution =
                Objects.requireNonNull(distribution);
        this.retry = Objects.requireNonNull(retry);
    }

    public String name() {
        return name;
    }

    public RetryConfig retry() {
        return retry;
    }

    public StochasticTime distribution() {
        return distribution;
    }

    // CDF del tentativo originale
    public double cdf(double time) {
        if (time < 0.0) {
            return 0.0;
        }
        return distribution.CDF(time);
    }

    // Tentativo originale senza timeout
    public Simple originalActivity(String suffix) {
        return new Simple(
                name + suffix,
                distribution.clone()
        );
    }

    // Tentativo condizionato: R | R <= timeout
    public Simple successfulAttempt(String activityName) {

        BigDecimal timeout = retry.timeout();

        if (timeout.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Il timeout deve essere positivo");
        }

        double p = cdf(timeout.doubleValue());

        if (!Double.isFinite(p) || p <= 0.0) {
            throw new IllegalArgumentException(
                    "Probabilita' di successo nulla o non valida");
        }

        var source = distribution
                .getStochasticTransitionFeature()
                .density();

        if (!(source instanceof Function function)) {
            throw new UnsupportedOperationException(
                    "Distribuzione non supportata");
        }

        // Copia per non alterare la distribuzione originale
        GEN conditioned = new GEN(function);

        // Troncamento e normalizzazione automatica
        conditioned.conditionToMax(
                Variable.X,
                new OmegaBigDecimal(timeout)
        );

        BigDecimal upper =
                timeout.min(distribution.getLFT());

        StochasticTime truncated =
                new ExpolynomialTime(
                        distribution.getEFT(),
                        upper,
                        conditioned.getDensity().toString()
                );

        return new Simple(activityName, truncated);
    }
}
