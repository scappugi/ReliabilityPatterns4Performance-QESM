package it.workflow.predictor.examples;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.UniformRetryTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ForkJoinUniformWorkflowRetry {
    private ForkJoinUniformWorkflowRetry() {}

    public static void main(String[] args) {
        List<UniformRetryTask> tasks = Arrays.asList(
                new UniformRetryTask("T1", new BigDecimal("1"), new BigDecimal("5"),
                        new RetryConfig(new BigDecimal("3"), 2)),
                new UniformRetryTask("T2", new BigDecimal("2"), new BigDecimal("3"),
                        new RetryConfig(new BigDecimal("2.5"), 1))
        );
        String bound = "15";
        String step = "0.05";
        List<RetryScenario> scenarios = ScenarioEnumerator.enumerate(tasks);
        List<double[]> cdfs = new ArrayList<>();

        System.out.println("Scenari totali: " + scenarios.size());
        for (RetryScenario scenario : scenarios) {
            System.out.println(scenario);
            cdfs.add(ScenarioEvaluator.evaluate(tasks, scenario,
                    ScenarioEvaluator.Composition.FORK_JOIN, bound, step, "0.0001"));
        }
        double[] finalCdf = ScenarioMixer.mix(scenarios, cdfs);
        int index = new BigDecimal("5.0").divide(new BigDecimal(step)).intValueExact();
        System.out.printf(Locale.US, "P(T_workflow <= 5 s) = %.6f%n", finalCdf[index]);
    }

}
