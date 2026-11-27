
package it.workflow.predictor.evaluation;

import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.DeterministicTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class OneShotTaskBuilder {

    private OneShotTaskBuilder() {}

    public static Activity build(RetryTask task) {

        int c = task.retry().maxRetries();
        BigDecimal timeout = task.retry().timeout();

        // Nessun retry: tentativo originale senza timeout
        if (c == 0) {
            return task.originalActivity("_original");
        }

        double p = task.cdf(timeout.doubleValue());

        if (!Double.isFinite(p) || p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException(
                    "Probabilita' di successo non valida");
        }

        List<Activity> branches = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        for (int n = 0; n <= c; n++) {

            double weight = Math.pow(1.0 - p, n);

            if (n < c) {
                weight *= p;
            }

            // Gli scenari con probabilita' nulla si ignorano
            if (weight <= 0.0) {
                continue;
            }

            Activity attempt;

            if (n < c) {
                // Ultimo tentativo riuscito entro timeout
                attempt = task.successfulAttempt(
                        task.name() + "_success_" + n);
            } else {
                // Esauriti i retry: tentativo senza timeout
                attempt = task.originalActivity(
                        "_final_" + n);
            }

            Activity branch = attempt;

            if (n > 0) {
                // Tempo consumato dai tentativi interrotti
                BigDecimal delay =
                        timeout.multiply(BigDecimal.valueOf(n));

                Activity delayActivity = new Simple(
                        task.name() + "_delay_" + n,
                        new DeterministicTime(delay)
                );

                branch = ModelFactory.sequence(
                        delayActivity,
                        attempt
                );
            }

            branches.add(branch);
            weights.add(weight);
        }

        if (branches.size() == 1) {
            return branches.get(0);
        }

        // Normalizzazione contro gli arrotondamenti
        double total = weights.stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        List<Double> normalized = new ArrayList<>();

        for (double weight : weights) {
            normalized.add(weight / total);
        }

        return ModelFactory.XOR(
                normalized,
                branches.toArray(new Activity[0])
        );
    }
}
