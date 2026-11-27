package it.workflow.predictor.workflowTypesExamples.HyperExponential;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;
import it.workflow.predictor.reliability.distributionTypes.HyperExponentialRetryTask;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

/** Sequential con retry, usando ScenarioEnumerator + ScenarioEvaluator + ScenarioMixer. */
public final class SequentialHyperExponentialRetry {
    private SequentialHyperExponentialRetry() {}

    public static void main(String[] args) {
        String bound = "20";
        String step = "0.05";
        String error = "0.0001";
        List<RetryTask> tasks = Arrays.asList(
                new HyperExponentialRetryTask(
                        "T1",
                        Arrays.asList(new BigDecimal("0.8"), new BigDecimal("0.2")),
                        Arrays.asList(new BigDecimal("2"), new BigDecimal("0.1")),
                        new RetryConfig(new BigDecimal("3"), 8)
                ),
                new HyperExponentialRetryTask(
                        "T2",
                        Arrays.asList(new BigDecimal("0.8"), new BigDecimal("0.2")),
                        Arrays.asList(new BigDecimal("1.5"), new BigDecimal("0.15")),
                        new RetryConfig(new BigDecimal("2.5"), 10)
                )
        );
        List<RetryScenario> scenarios = ScenarioEnumerator.enumerate(tasks);
        List<double[]> cdfs = new ArrayList<>();
        System.out.println("Scenari totali: " + scenarios.size());
        for (RetryScenario scenario : scenarios) {
            System.out.println(scenario);
            cdfs.add(ScenarioEvaluator.evaluate(tasks, scenario,
                    ScenarioEvaluator.Composition.SEQUENCE, bound, step, error));
        }
        double[] finalCdf = ScenarioMixer.mix(scenarios, cdfs);
        int index = new BigDecimal("5.0").divide(new BigDecimal(step)).intValueExact();
        System.out.printf(Locale.US, "P(T_workflow <= 5 s) = %.6f%n", finalCdf[index]);

        try {
            Path output = Path.of("python/hyperexponential/sequential_hyperexponential_retry_cdf.csv");
            Files.createDirectories(output.getParent());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(output))) {
                writer.println("time,cdf");
                double stepValue = new BigDecimal(step).doubleValue();
                for (int i = 0; i < finalCdf.length; i++) {
                    writer.printf(Locale.US, "%.5f,%.8f%n", i * stepValue, finalCdf[i]);
                }
                if (writer.checkError()) throw new IOException("Errore scrittura CSV");
            }
            System.out.println("CDF esportata in: " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
