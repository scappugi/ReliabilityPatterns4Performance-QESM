package it.workflow.predictor.workflowTypesExamples.Uniform;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.UniformRetryTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Workflow Choice:
 * T1 ~ Uniforme(1,5)
 * T2 ~ Uniforme(2,3)
 *
 * Viene scelto un solo task, con probabilità 0.5.
 * Entrambi i task possono avere retry.
 */
public final class ChoiceUniformRetry {

    private ChoiceUniformRetry() {}

    public static void main(String[] args) {

        // 1. Definizione dei task con le rispettive configurazioni di retry

        List<UniformRetryTask> tasksT1 = Arrays.asList(
                new UniformRetryTask(
                        "T1",
                        new BigDecimal("1"),
                        new BigDecimal("5"),
                        new RetryConfig(new BigDecimal("3"), 2)
                ),
        new UniformRetryTask(
                "T3",
                new BigDecimal("2"),
                new BigDecimal("7"),
                new RetryConfig(new BigDecimal("5"), 2)
        ),
        new UniformRetryTask(
                "T4",
                new BigDecimal("5"),
                new BigDecimal("8"),
                new RetryConfig(new BigDecimal("6"), 2)
        )
        );

        List<UniformRetryTask> tasksT2 = Arrays.asList(
                new UniformRetryTask(
                        "T2",
                        new BigDecimal("2"),
                        new BigDecimal("3"),
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
                            ScenarioEvaluator.Composition.FORK_JOIN,
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

        // Verifica delle probabilita' dei rami
        if (Math.abs(probabilityT1 + probabilityT2 - 1.0) > 1e-8) {
            throw new IllegalArgumentException(
                    "Le probabilita' della choice non sommano a 1"
            );
        }

        // Le CDF devono avere la stessa griglia temporale
        if (finalCdfT1.length != finalCdfT2.length) {
            throw new IllegalArgumentException(
                    "Le CDF hanno lunghezze diverse"
            );
        }

        int length = finalCdfT1.length;
        double[] finalCdf = new double[length];

        // Mistura delle CDF dei due rami
        for (int i = 0; i < length; i++) {

            finalCdf[i] =
                    probabilityT1 * finalCdfT1[i]
                            + probabilityT2 * finalCdfT2[i];
        }

        // 7. Probabilità di completamento entro 5 secondi

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
                     new PrintWriter("python/choice_retry_cdf.csv")) {

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
                    "CDF esportata in choice_retry_cdf.csv"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
