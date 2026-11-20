package it.workflow.predictor.evaluation;

import it.workflow.predictor.reliability.distributionTypes.RetryTask;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.XOR;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * One-shot: sostituisce un task con la XOR delle possibili durate dovute ai retry.
 * Equazioni (1)-(3) del progetto: i rami hanno pesi D_n e durata n*timeout+R_cond,
 * salvo l'ultimo (n=c), che termina con la distribuzione originale non troncata.
 *
 * Non enumera scenari GLOBALI e non esegue analisi Eulero.
 */
public final class OneShotTaskBuilder {
    private OneShotTaskBuilder() {}

    public static Activity build(RetryTask task) {
        if (task == null || task.retry() == null) {
            throw new IllegalArgumentException("Task e RetryConfig obbligatori");
        }
        final int c = task.retry().maxRetries();
        final BigDecimal timeout = task.retry().timeout();
        if (c < 0 || timeout == null || timeout.signum() <= 0) {
            throw new IllegalArgumentException("Retry massimi o timeout non validi");
        }
        // Nessun retry: non serve neppure un nodo XOR.
        if (c == 0) {
            return task.originalActivity("_oneShot_original");
        }

        final double p = task.cdf(timeout.doubleValue());
        if (!Double.isFinite(p) || p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("F(timeout) non valida: " + p);
        }

        List<Activity> branches = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double timeoutProbability = 1.0 - p;
        double previousTimeoutsProbability = 1.0;

        for (int n = 0; n <= c; n++) {
            double weight = n < c
                    ? previousTimeoutsProbability * p
                    : previousTimeoutsProbability;

            // Escludere rami impossibili evita di costruire una condizionata se F(timeout)=0.
            if (weight > 0.0) {
                String branchName = task.name() + "_oneShot_retry" + n;
                Activity lastAttempt = (n < c)
                        ? task.successfulAttempt(branchName + "_success")
                        : task.originalActivity("_oneShot_last" + n);

                Activity branch;
                if (n == 0) {
                    branch = lastAttempt;
                } else {
                    BigDecimal delay = timeout.multiply(BigDecimal.valueOf(n));
                    Activity delayActivity = new Simple(
                            branchName + "_delay",
                            StochasticTransitionFeature.newDeterministicInstance(delay)
                    );
                    branch = DAG.sequence(branchName, delayActivity, lastAttempt);
                }
                branches.add(branch);
                weights.add(weight);
            }
            previousTimeoutsProbability *= timeoutProbability;
        }

        if (branches.isEmpty()) {
            throw new IllegalStateException("Nessun ramo di retry valido per " + task.name());
        }
        if (branches.size() == 1) {
            return branches.get(0);
        }

        // Riduce gli errori di arrotondamento nei pesi (la somma teorica e' 1).
        double sum = 0.0;
        for (double w : weights) sum += w;
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) / sum);
        }
        return new XOR(task.name() + "_oneShot_XOR", branches, weights);
    }
}
