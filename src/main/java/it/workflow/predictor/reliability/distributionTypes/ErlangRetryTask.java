
package it.workflow.predictor.reliability.distributionTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Objects;

import it.workflow.predictor.reliability.RetryConfig;
import org.apache.commons.math3.special.Gamma;
import org.oristool.eulero.modeling.Simple;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

/**
 * Task con distribuzione Erlang(k, lambda)
 * e politica di timeout/retry.
 */
public final class ErlangRetryTask implements RetryTask {

    private final String name;
    private final int k;
    private final BigDecimal lambda;
    private final RetryConfig retry;

    public ErlangRetryTask(
            String name,
            int k,
            BigDecimal lambda,
            RetryConfig retry) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Nome del task obbligatorio"
            );
        }

        if (k < 1) {
            throw new IllegalArgumentException(
                    "k deve essere >= 1"
            );
        }

        Objects.requireNonNull(lambda, "lambda");
        Objects.requireNonNull(retry, "retry");

        if (lambda.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Lambda deve essere positivo"
            );
        }

        this.name = name;
        this.k = k;
        this.lambda = lambda;
        this.retry = retry;
    }

    @Override
    public String name() {
        return name;
    }

    public int k() {
        return k;
    }

    public BigDecimal lambda() {
        return lambda;
    }

    @Override
    public RetryConfig retry() {
        return retry;
    }

    /**
     * CDF originale Erlang.
     *
     * F(x) = P(k, lambda*x)
     *
     * P e' la funzione Gamma incompleta regolarizzata.
     */
    @Override
    public double cdf(double time) {

        if (time <= 0.0) {
            return 0.0;
        }

        double x = lambda.doubleValue() * time;

        return Gamma.regularizedGammaP(k, x);
    }

    /**
     * Distribuzione Erlang originale.
     * Usata nell'ultimo tentativo senza timeout.
     */
    @Override
    public Simple originalActivity(String suffix) {

        return Simple.erlang(
                name + suffix,
                k,
                lambda
        );
    }

    /**
     * Distribuzione condizionata al successo:
     *
     * R | R <= timeout
     *
     * f_cond(x) = f(x) / F(timeout)
     * per 0 <= x <= timeout.
     */
    @Override
    public Simple successfulAttempt(String activityName) {

        BigDecimal timeout = retry.timeout();

        double p = cdf(timeout.doubleValue());

        if (!Double.isFinite(p) || p <= 0.0) {
            throw new IllegalArgumentException(
                    "Probabilita' di successo nulla o non valida"
            );
        }

        // Coefficiente della densita' Erlang:
        // lambda^k / (k-1)!
        BigDecimal numerator = lambda.pow(k);

        BigInteger factorial = BigInteger.ONE;

        for (int i = 2; i <= k - 1; i++) {
            factorial = factorial.multiply(
                    BigInteger.valueOf(i)
            );
        }

        // Normalizzazione rispetto a F(timeout)
        BigDecimal denominator =
                new BigDecimal(factorial)
                        .multiply(BigDecimal.valueOf(p));

        BigDecimal coefficient = numerator.divide(
                denominator,
                MathContext.DECIMAL128
        );

        // Densita':
        // coefficient * x^(k-1) * Exp[-lambda x]
        String density =
                coefficient.toPlainString();

        if (k > 1) {
            density += "*x^" + (k - 1);
        }

        density += "*Exp[-"
                + lambda.toPlainString()
                + " x]";

        // Distribuzione troncata nell'intervallo
        // [0, timeout]
        StochasticTransitionFeature feature =
                StochasticTransitionFeature.newExpolynomial(
                        density,
                        OmegaBigDecimal.ZERO,
                        new OmegaBigDecimal(timeout)
                );

        return new Simple(activityName, feature);
    }
}
