
package it.workflow.predictor.evaluation;

import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.DeterministicTime;

import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

/**
 * Eq. (3): costruisce il workflow dello scenario,
 * senza XOR per i retry.
 */
public final class ScenarioEvaluator {

    private ScenarioEvaluator() {}

    public enum Composition {
        SEQUENCE,
        FORK_JOIN
    }

    /**
     * Rimpiazza un task con:
     * n * timeout + tentativo finale
     * (condizionato oppure originale).
     */
    public static Activity taskInScenario(RetryTask task, int n) {

        int c = task.retry().maxRetries();

        if (n < 0 || n > c) {
            throw new IllegalArgumentException("Scenario non valido");
        }

        String id = task.name() + "_retry" + n;

        Activity lastAttempt = n < c
                ? task.successfulAttempt(id + "_success")
                : task.originalActivity("_last_retry" + n);

        if (n == 0) {
            return lastAttempt;
        }

        // Ritardo deterministico: n timeout già trascorsi
        BigDecimal delayDuration = task.retry()
                .timeout()
                .multiply(BigDecimal.valueOf(n));

        Simple delay = new Simple(
                id + "_delay",
                new DeterministicTime(delayDuration)
        );

        return ModelFactory.sequence(delay, lastAttempt);
    }

    public static double[] evaluate(
            List<? extends RetryTask> tasks,
            RetryScenario scenario,
            Composition composition,
            String timeBound,
            String timeStep,
            String error) {

        List<Activity> activities = new ArrayList<>();

        for (RetryTask task : tasks) {
            activities.add(
                    taskInScenario(
                            task,
                            scenario.retryCount(task.name())
                    )
            );
        }

        Activity[] nodes = activities.toArray(new Activity[0]);

        Activity workflow = composition == Composition.FORK_JOIN
                ? ModelFactory.forkJoin(nodes)
                : ModelFactory.sequence(nodes);

        TransientSolution<DeterministicEnablingState, RewardRate> solution =
                workflow.analyze(timeBound, timeStep, error);

        double[][][] raw = solution.getSolution();
        double[] cdf = new double[raw.length];

        for (int i = 0; i < cdf.length; i++) {
            cdf[i] = raw[i][0][0];
        }

        return cdf;
    }
}
