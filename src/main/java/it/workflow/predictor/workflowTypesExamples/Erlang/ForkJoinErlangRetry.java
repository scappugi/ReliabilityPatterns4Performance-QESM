package it.workflow.predictor.workflowTypesExamples.Erlang;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.ErlangRetryTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.math.BigDecimal;
import java.util.Locale;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/** T1 ~ Erlang(2,1), T2 ~ Erlang(3,1.5), ForkJoin con retry. */
public final class ForkJoinErlangRetry {

    private ForkJoinErlangRetry() {}

    public static void main(String[] args) {

        List<RetryTask> tasks = Arrays.asList(
                new ErlangRetryTask("T1", 2, new BigDecimal("1"),
                        new RetryConfig(new BigDecimal("3"), 2)),
                new ErlangRetryTask("T2", 3, new BigDecimal("1.5"),
                        new RetryConfig(new BigDecimal("2.5"), 1))
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // 1. Enumerazione scenari di retry.
        List<RetryScenario> scenarios = ScenarioEnumerator.enumerate(tasks);
        List<double[]> cdfs = new ArrayList<>();

        System.out.println("Scenari totali: " + scenarios.size());

        // 2. Valutazione di ogni scenario con Eulero.
        for (RetryScenario scenario : scenarios) {
            System.out.println(scenario);
            cdfs.add(ScenarioEvaluator.evaluate(tasks, scenario,
                    ScenarioEvaluator.Composition.FORK_JOIN,
                    bound, step, error));
        }

        // 3. Mistura dei sei scenari.
        double[] finalCdf = ScenarioMixer.mix(scenarios, cdfs);

        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step)).intValueExact();
        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n", finalCdf[index]);

        // Esportazione della CDF per Python.
        try {
            Path output = Path.of("python/erlang/fork_join_erlang_retry_cdf.csv");
            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {
                writer.println("time,cdf");
                double timeStep = new BigDecimal(step).doubleValue();

                for (int i = 0; i < finalCdf.length; i++) {
                    writer.printf(Locale.US, "%.5f,%.8f%n",
                            i * timeStep, finalCdf[i]);
                }

                if (writer.checkError()) {
                    throw new IOException("Errore durante la scrittura del CSV");
                }
            }

            System.out.println("CDF esportata in " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
