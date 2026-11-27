package it.workflow.predictor.workflowTypesExamples.Mix;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;
import org.oristool.eulero.modeling.stochastictime.ExponentialTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ForkJoinMixRetry {

    private ForkJoinMixRetry() {}

    public static void main(String[] args) {

        /*List<RetryTask> tasks = Arrays.asList(
                new UniformRetryTask("T1", new BigDecimal("1"), new BigDecimal("5"),
                        new RetryConfig(new BigDecimal("3"), 2)),
                new ExponentialRetryTask("T2", new BigDecimal("2"),
                        new RetryConfig(new BigDecimal("2.5"), 1))
        );*/

        List<RetryTask> tasks = Arrays.asList(

                new RetryTask(
                        "T1",
                        new UniformTime(
                                new BigDecimal("1"),
                                new BigDecimal("5")
                        ),
                        new RetryConfig(new BigDecimal("3"), 2)
                ),

                new RetryTask(
                        "T2",
                        new ExponentialTime(
                                new BigDecimal("2")
                        ),
                        new RetryConfig(new BigDecimal("2.5"), 1)
                )
        );

        String bound = "20";
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

        try (PrintWriter writer =
                     new PrintWriter("python/mix/fork_join_mix_retry_cdf.csv")) {

            // Intestazione del file CSV
            writer.println("time,cdf");

            double timeStep = new BigDecimal(step).doubleValue();

            // Esportiamo tutta la CDF
            for (int i = 0; i < finalCdf.length; i++) {

                double time = i * timeStep;
                double probability = finalCdf[i];

                writer.printf(
                        Locale.US,
                        "%.5f,%.8f%n",
                        time,
                        probability
                );
            }

            System.out.println(
                    "CDF esportata in fork_join_mix_retry_cdf.csv"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
