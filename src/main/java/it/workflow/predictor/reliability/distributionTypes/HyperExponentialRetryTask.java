package it.workflow.predictor.reliability.distributionTypes;

import it.workflow.predictor.reliability.RetryConfig;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Un tentativo segue una mistura di esponenziali; ad ogni retry la mistura viene ricampionata. */
public final class HyperExponentialRetryTask implements RetryTask {

    private final String name;
    private final List<BigDecimal> probabilities;
    private final List<BigDecimal> rates;
    private final RetryConfig retry;

    public HyperExponentialRetryTask(String name,
                                     List<BigDecimal> probabilities,
                                     List<BigDecimal> rates,
                                     RetryConfig retry) {
        if (name == null || name.isEmpty() || retry == null
                || probabilities == null || rates == null
                || probabilities.size() < 2 || probabilities.size() != rates.size()) {
            throw new IllegalArgumentException("Nome, retry e componenti iperesponenziali non validi");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < probabilities.size(); i++) {
            BigDecimal q = probabilities.get(i);
            BigDecimal lambda = rates.get(i);
            if (q == null || lambda == null || q.signum() <= 0 || lambda.signum() <= 0) {
                throw new IllegalArgumentException("Probabilita' e tassi devono essere positivi");
            }
            total = total.add(q);
        }
        if (total.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("Le probabilita' devono sommare esattamente a 1");
        }
        if (retry.timeout().signum() <= 0) {
            throw new IllegalArgumentException("Il timeout deve essere positivo");
        }
        this.name = name;
        this.probabilities = Collections.unmodifiableList(new ArrayList<>(probabilities));
        this.rates = Collections.unmodifiableList(new ArrayList<>(rates));
        this.retry = retry;
    }

    @Override
    public String name() { return name; }

    @Override
    public RetryConfig retry() { return retry; }

    /** F(t) = sum_j q_j (1 - exp(-lambda_j*t)). */
    @Override
    public double cdf(double time) {
        if (time <= 0.0) return 0.0;
        double result = 0.0;
        for (int i = 0; i < rates.size(); i++) {
            result += probabilities.get(i).doubleValue()
                    * (-Math.expm1(-rates.get(i).doubleValue() * time));
        }
        return Math.max(0.0, Math.min(1.0, result));
    }

    /** Distribuzione originale del tentativo, senza troncamento. */
    @Override
    public Activity originalActivity(String suffix) {
        return new Simple(name + suffix,
                StochasticTransitionFeature.newHyperExp(probabilities, rates));
    }

    /** Densita' condizionata f(t)/F(timeout), per 0 <= t <= timeout. */
    @Override
    public Activity successfulAttempt(String activityName) {
        BigDecimal timeout = retry.timeout();
        double p = cdf(timeout.doubleValue());
        if (!(p > 0.0)) {
            throw new IllegalArgumentException("F(timeout) deve essere positivo");
        }
        BigDecimal denominator = BigDecimal.valueOf(p);
        StringBuilder density = new StringBuilder();
        for (int i = 0; i < rates.size(); i++) {
            if (i > 0) density.append(" + ");
            BigDecimal coefficient = probabilities.get(i).multiply(rates.get(i))
                    .divide(denominator, MathContext.DECIMAL128);
            density.append(coefficient.toPlainString())
                    .append(" * Exp[-")
                    .append(rates.get(i).toPlainString())
                    .append(" x]");
        }
        StochasticTransitionFeature feature = StochasticTransitionFeature.newExpolynomial(
                density.toString(), OmegaBigDecimal.ZERO,
                new OmegaBigDecimal(timeout));
        return new Simple(activityName, feature);
    }
}
