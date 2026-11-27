
package it.workflow.predictor.workflowTypesExamples.Exponential;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;

import org.oristool.eulero.modeling.stochastictime.ExponentialTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Sequential con retry.
 * T1 ~ Exp(0.5), timeout=3, maxRetries=2
 * T2 ~ Exp(0.8), timeout=2.5, maxRetries=1
 */
public final class SequentialExponentialRetry {

    private SequentialExponentialRetry() {}

    public static void main(String[] args) {

        // 1. Definizione dei task con retry
        List<RetryTask> tasks = Arrays.asList(

                new RetryTask(
                        "T1",
                        new ExponentialTime(new BigDecimal("0.5")),
                        new RetryConfig(
                                new BigDecimal("3"),
                                2
                        )
                ),

                new RetryTask(
                        "T2",
                        new ExponentialTime(new BigDecimal("0.8")),
                        new RetryConfig(
                                new BigDecimal("2.5"),
                                1
                        )
                )
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // 2. Enumerazione degli scenari
        List<RetryScenario> scenarios =
                ScenarioEnumerator.enumerate(tasks);

        List<double[]> cdfs = new ArrayList<>();

        System.out.println(
                "Scenari totali: " + scenarios.size()
        );

        // 3. Eulero valuta ogni scenario
        for (RetryScenario scenario : scenarios) {

            System.out.println(scenario);

            cdfs.add(
                    ScenarioEvaluator.evaluate(
                            tasks,
                            scenario,
                            ScenarioEvaluator.Composition.SEQUENCE,
                            bound,
                            step,
                            error
                    )
            );
        }

        // 4. Mistura delle CDF degli scenari
        double[] finalCdf =
                ScenarioMixer.mix(scenarios, cdfs);

        // 5. Probabilita' entro 5 secondi
        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step))
                .intValueExact();

        System.out.printf(
                Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                finalCdf[index]
        );

        // 6. Esportazione dell'intera CDF
        try {
            Path output = Path.of(
                    "python/exponential/sequence_exponential_retry_cdf.csv"
            );

            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {

                writer.println("time,cdf");

                double timeStep =
                        new BigDecimal(step).doubleValue();

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

                if (writer.checkError()) {
                    throw new IOException(
                            "Errore di scrittura del CSV"
                    );
                }
            }

            System.out.println("CDF esportata in: " + output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
