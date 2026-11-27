
package it.workflow.predictor.workflowTypesExamples.HyperExponential;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import org.oristool.eulero.modeling.stochastictime.HyperExponentialTime;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

/** Choice con retry, usando ScenarioEnumerator + ScenarioEvaluator + ScenarioMixer. */
public final class ChoiceHyperExponentialRetry {

    private ChoiceHyperExponentialRetry() {}

    public static void main(String[] args) {

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // Scenari separati per ogni ramo,
        // poi mistura manuale 50/50.
        // La Choice avviene una sola volta.

        List<RetryTask> tasksT1 = Arrays.asList(
                new RetryTask(
                        "T1",
                        new HyperExponentialTime(
                                new BigDecimal("2"),
                                new BigDecimal("0.1"),
                                new BigDecimal("0.8")
                        ),
                        new RetryConfig(new BigDecimal("3"), 2)
                )
        );

        List<RetryTask> tasksT2 = Arrays.asList(
                new RetryTask(
                        "T2",
                        new HyperExponentialTime(
                                new BigDecimal("1.5"),
                                new BigDecimal("0.15"),
                                new BigDecimal("0.8")
                        ),
                        new RetryConfig(new BigDecimal("2.5"), 1)
                )
        );

        List<RetryScenario> scenariosT1 =
                ScenarioEnumerator.enumerate(tasksT1);

        List<double[]> cdfsT1 = new ArrayList<>();

        for (RetryScenario scenario : scenariosT1) {

            System.out.println("T1: " + scenario);

            cdfsT1.add(
                    ScenarioEvaluator.evaluate(
                            tasksT1,
                            scenario,
                            ScenarioEvaluator.Composition.SEQUENCE,
                            bound,
                            step,
                            error
                    )
            );
        }

        double[] finalCdfT1 =
                ScenarioMixer.mix(scenariosT1, cdfsT1);

        List<RetryScenario> scenariosT2 =
                ScenarioEnumerator.enumerate(tasksT2);

        List<double[]> cdfsT2 = new ArrayList<>();

        for (RetryScenario scenario : scenariosT2) {

            System.out.println("T2: " + scenario);

            cdfsT2.add(
                    ScenarioEvaluator.evaluate(
                            tasksT2,
                            scenario,
                            ScenarioEvaluator.Composition.SEQUENCE,
                            bound,
                            step,
                            error
                    )
            );
        }

        double[] finalCdfT2 =
                ScenarioMixer.mix(scenariosT2, cdfsT2);

        double probabilityT1 = 0.5;
        double probabilityT2 = 0.5;

        if (Math.abs(probabilityT1 + probabilityT2 - 1.0) > 1e-8) {
            throw new IllegalArgumentException(
                    "Le probabilita' della choice non sommano a 1"
            );
        }

        if (finalCdfT1.length != finalCdfT2.length) {
            throw new IllegalArgumentException(
                    "Le CDF hanno lunghezze diverse"
            );
        }

        double[] finalCdf = new double[finalCdfT1.length];

        for (int i = 0; i < finalCdf.length; i++) {
            finalCdf[i] =
                    probabilityT1 * finalCdfT1[i]
                            + probabilityT2 * finalCdfT2[i];
        }

        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step))
                .intValueExact();

        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                finalCdf[index]);

        try {
            Path output = Path.of(
                    "python/hyperexponential/choice_hyperexponential_retry_cdf.csv"
            );

            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {

                writer.println("time,cdf");

                double stepValue =
                        new BigDecimal(step).doubleValue();

                for (int i = 0; i < finalCdf.length; i++) {
                    writer.printf(Locale.US, "%.5f,%.8f%n",
                            i * stepValue, finalCdf[i]);
                }

                if (writer.checkError()) {
                    throw new IOException("Errore scrittura CSV");
                }
            }

            System.out.println("CDF esportata in: " + output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
