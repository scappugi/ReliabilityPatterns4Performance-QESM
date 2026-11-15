package it.workflow.predictor.reliability.distributionTypes;

import it.workflow.predictor.reliability.RetryConfig;
import org.oristool.eulero.modeling.Activity;

public interface RetryTask {

    String name();

    RetryConfig retry();

    // CDF originale F_i(x)
    double cdf(double time);

    // Attivita' Eulero senza timeout
    Activity originalActivity(String suffix);

    // Attivita' Eulero condizionata al successo
    Activity successfulAttempt(String name);
}
