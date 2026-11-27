
package it.workflow.predictor.workflowTypesExamples.Exponential;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import org.oristool.eulero.modeling.stochastictime.ExponentialTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Workflow Choice:
 *
 * T1 ~ Exp(0.5)
 * T2 ~ Exp(0.8)
 *
 * Viene scelto un solo task, con probabilita' 0.5.
 * Entrambi i task possono avere retry.
 */
public final class ChoiceExponentialRetry {

    private ChoiceExponentialRetry() {}

    public static void main(String[] args) {

        // 1. Definizione dei task con le rispettive configurazioni di retry

        List<RetryTask> tasksT1 = Arrays.asList(
                new RetryTask(
                        "T1",
                        new ExponentialTime(new BigDecimal("0.5")),
                        new RetryConfig(new BigDecimal("3"), 2)
                )
        );

        List<RetryTask> tasksT2 = Arrays.asList(
                new RetryTask(
                        "T2",
                        new ExponentialTime(new BigDecimal("0.8")),
                        new RetryConfig(new BigDecimal("2.5"), 1)
                )
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // 2. Enumerazione degli scenari di retry per T1

        List<RetryScenario> scenariosT1 =
                ScenarioEnumerator.enumerate(tasksT1);

        List<double[]> cdfsT1 = new ArrayList<>();

        System.out.println("Scenari di retry T1: " + scenariosT1.size());

        for (RetryScenario scenario : scenariosT1) {

            System.out.println(scenario);

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

        // 3. Combinazione degli scenari di retry di T1

        double[] finalCdfT1 =
                ScenarioMixer.mix(scenariosT1, cdfsT1);

        // 4. Enumerazione degli scenari di retry per T2

        List<RetryScenario> scenariosT2 =
                ScenarioEnumerator.enumerate(tasksT2);

        List<double[]> cdfsT2 = new ArrayList<>();

        System.out.println("Scenari di retry T2: " + scenariosT2.size());

        for (RetryScenario scenario : scenariosT2) {

            System.out.println(scenario);

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

        // 5. Combinazione degli scenari di retry di T2

        double[] finalCdfT2 =
                ScenarioMixer.mix(scenariosT2, cdfsT2);

        // 6. Choice tra T1 e T2

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

        int length = finalCdfT1.length;
        double[] finalCdf = new double[length];

        for (int i = 0; i < length; i++) {

            finalCdf[i] =
                    probabilityT1 * finalCdfT1[i]
                            + probabilityT2 * finalCdfT2[i];
        }

        // 7. Probabilita' di completamento entro 5 secondi

        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step))
                .intValueExact();

        System.out.printf(
                Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                finalCdf[index]
        );

        // 8. Esportazione dell'intera CDF

        try (PrintWriter writer =
                     new PrintWriter("python/exponential/choice_exponential_retry_cdf.csv")) {

            writer.println("time,cdf");

            double timeStep = new BigDecimal(step).doubleValue();

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
                    "CDF esportata in choice_exponential_retry_cdf.csv"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
