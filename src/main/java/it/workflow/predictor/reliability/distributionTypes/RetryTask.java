package it.workflow.predictor.reliability.distributionTypes;

import it.workflow.predictor.reliability.RetryConfig;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.StochasticTime;

import java.util.Objects;

/*public interface RetryTask {

    String name();

    RetryConfig retry();

    // CDF originale F_i(x)
    double cdf(double time);

    // Attivita' Eulero senza timeout
    Activity originalActivity(String suffix);

    // Attivita' Eulero condizionata al successo
    Activity successfulAttempt(String name);
}*/



public final class RetryTask {

    private final String name;
    private final StochasticTime distribution;
    private final RetryConfig retry;

    public RetryTask(
            String name,
            StochasticTime distribution,
            RetryConfig retry) {

        this.name = Objects.requireNonNull(name);
        this.distribution = Objects.requireNonNull(distribution);
        this.retry = Objects.requireNonNull(retry);
    }

    public String name() {
        return name;
    }

    public RetryConfig retry() {
        return retry;
    }

    public double cdf(double time) {
        return distribution.CDF(time);
    }

    public Simple originalActivity(String suffix) {
        return new Simple(
                name + suffix,
                distribution.clone()
        );
    }

    public StochasticTime distribution() {
        return distribution;
    }
}