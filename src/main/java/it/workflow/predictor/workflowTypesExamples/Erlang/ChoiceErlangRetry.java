
package it.workflow.predictor.workflowTypesExamples.Erlang;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import org.oristool.eulero.modeling.stochastictime.ErlangTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.math.BigDecimal;
import java.util.Locale;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Choice Erlang con retry (Scenario Enumeration).
 *
 * T1 ~ Erlang(2, 1), timeout=3, maxRetries=2
 * T2 ~ Erlang(3, 1.5), timeout=2.5, maxRetries=1
 *
 * Choice tra T1 e T2 con probabilita' 0.5 ciascuno.
 */
public final class ChoiceErlangRetry {

    private ChoiceErlangRetry() {}

    public static void main(String[] args) {

        // 1. Definizione dei task

        List<RetryTask> tasksT1 = Arrays.asList(
                new RetryTask(
                        "T1",
                        new ErlangTime(2, 1.0),
                        new RetryConfig(new BigDecimal("3"), 2)
                )
        );

        List<RetryTask> tasksT2 = Arrays.asList(
                new RetryTask(
                        "T2",
                        new ErlangTime(3, 1.5),
                        new RetryConfig(new BigDecimal("2.5"), 1)
                )
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // 2. Scenari di retry del primo ramo: T1

        List<RetryScenario> scenariosT1 =
                ScenarioEnumerator.enumerate(tasksT1);

        List<double[]> cdfsT1 = new ArrayList<>();

        System.out.println("Scenari del ramo T1: "
                + scenariosT1.size());

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

        double[] finalCdfT1 =
                ScenarioMixer.mix(scenariosT1, cdfsT1);

        // 3. Scenari di retry del secondo ramo: T2

        List<RetryScenario> scenariosT2 =
                ScenarioEnumerator.enumerate(tasksT2);

        List<double[]> cdfsT2 = new ArrayList<>();

        System.out.println("Scenari del ramo T2: "
                + scenariosT2.size());

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

        double[] finalCdfT2 =
                ScenarioMixer.mix(scenariosT2, cdfsT2);

        // 4. Choice 50%-50% tra T1 e T2

        double probabilityT1 = 0.5;
        double probabilityT2 = 0.5;

        if (Math.abs(probabilityT1 + probabilityT2 - 1.0) > 1e-8) {
            throw new IllegalArgumentException(
                    "Le probabilita' dei rami non sommano a 1");
        }

        if (finalCdfT1.length != finalCdfT2.length) {
            throw new IllegalArgumentException(
                    "Le griglie temporali non coincidono");
        }

        double[] finalCdf = new double[finalCdfT1.length];

        for (int i = 0; i < finalCdf.length; i++) {
            finalCdf[i] =
                    probabilityT1 * finalCdfT1[i]
                            + probabilityT2 * finalCdfT2[i];
        }

        // 5. Probabilita' di completamento entro 5 secondi

        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step))
                .intValueExact();

        System.out.printf(
                Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                finalCdf[index]
        );

        // 6. Esportazione della CDF per Python

        try {
            Path output = Path.of(
                    "python/erlang/choice_erlang_retry_cdf.csv"
            );

            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {

                writer.println("time,cdf");

                double timeStep =
                        new BigDecimal(step).doubleValue();

                for (int i = 0; i < finalCdf.length; i++) {
                    writer.printf(
                            Locale.US,
                            "%.5f,%.8f%n",
                            i * timeStep,
                            finalCdf[i]
                    );
                }

                if (writer.checkError()) {
                    throw new IOException(
                            "Errore durante la scrittura del CSV"
                    );
                }
            }

            System.out.println("CDF esportata in " + output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
