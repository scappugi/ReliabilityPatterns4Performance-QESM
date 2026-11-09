package it.workflow.predictor.evaluation;

import it.workflow.predictor.reliability.ConditionalDistribution;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.UniformRetryTask;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;

/** Eq. (3): costruisce in Eulero il workflow dello scenario, SENZA XOR per i retry. */
public final class ScenarioEvaluator {
    private ScenarioEvaluator() {}

    public enum Composition { SEQUENCE, FORK_JOIN }

    /** Rimpiazza un task con: n*timeout + tentativo finale (condizionato oppure originale). */
    public static Activity taskInScenario(UniformRetryTask task, int n) {
        int c = task.retry().maxRetries();
        if (n < 0 || n > c) throw new IllegalArgumentException("Scenario non valido");
        String id = task.name() + "_retry" + n;

        Activity lastAttempt = n < c
            ? ConditionalDistribution.uniformSuccess(task, id + "_success")
            : task.originalActivity("_last_retry" + n);

        if (n == 0) return lastAttempt;

        // Eq. (3): ritardo deterministico per i timeout gia' trascorsi
        BigDecimal delayDuration = task.retry().timeout().multiply(BigDecimal.valueOf(n));
        Simple delay = new Simple(id + "_delay",
            StochasticTransitionFeature.newDeterministicInstance(delayDuration));
        return DAG.sequence(id, delay, lastAttempt);
    }

    public static double[] evaluate(List<UniformRetryTask> tasks,
                                    RetryScenario scenario,
                                    Composition composition,
                                    String timeBound, String timeStep, String error) {
        List<Activity> activities = new ArrayList<>();
        for (UniformRetryTask task : tasks) {
            activities.add(taskInScenario(task, scenario.retryCount(task.name())));
        }
        Activity[] nodes = activities.toArray(new Activity[0]);
        Activity workflow = composition == Composition.FORK_JOIN
            ? DAG.forkJoin("scenario_" + scenario.retryCounts(), nodes)
            : DAG.sequence("scenario_" + scenario.retryCounts(), nodes);

        TransientSolution<DeterministicEnablingState, RewardRate> solution =
            workflow.analyze(timeBound, timeStep, error);
        double[][][] raw = solution.getSolution();
        double[] cdf = new double[raw.length];
        for (int i = 0; i < cdf.length; i++) cdf[i] = raw[i][0][0];
        return cdf;
    }
}
