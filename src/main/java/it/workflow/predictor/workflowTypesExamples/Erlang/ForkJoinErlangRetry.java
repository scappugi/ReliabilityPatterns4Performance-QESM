
package it.workflow.predictor.workflowTypesExamples.Erlang;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;

import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import org.oristool.eulero.modeling.stochastictime.ErlangTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fork/Join Erlang con retry (Scenario Enumeration).
 *
 * T1 ~ Erlang(2, 1), timeout=3, maxRetries=2
 * T2 ~ Erlang(3, 1.5), timeout=2.5, maxRetries=1
 */
public final class ForkJoinErlangRetry {

    private ForkJoinErlangRetry() {}

    public static void main(String[] args) {

        // 1. Definizione dei task con RetryTask e StochasticTime

        List<RetryTask> tasks = List.of(
                new RetryTask(
                        "T1",
                        new ErlangTime(2, 1.0),
                        new RetryConfig(new BigDecimal("3"), 2)
                ),
                new RetryTask(
                        "T2",
                        new ErlangTime(3, 1.5),
                        new RetryConfig(new BigDecimal("2.5"), 1)
                )
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // 2. Enumerazione degli scenari di retry
        List<RetryScenario> scenarios =
                ScenarioEnumerator.enumerate(tasks);

        List<double[]> cdfs = new ArrayList<>();

        System.out.println(
                "Scenari totali: " + scenarios.size()
        );

        // 3. Valutazione di ogni scenario con Eulero
        for (RetryScenario scenario : scenarios) {

            System.out.println(scenario);

            cdfs.add(
                    ScenarioEvaluator.evaluate(
                            tasks,
                            scenario,
                            ScenarioEvaluator.Composition.FORK_JOIN,
                            bound,
                            step,
                            error
                    )
            );
        }

        // 4. Mistura delle CDF degli scenari
        double[] finalCdf =
                ScenarioMixer.mix(scenarios, cdfs);

        // 5. Probabilita' di completamento entro 5 secondi
        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step))
                .intValueExact();

        System.out.printf(
                Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                finalCdf[index]
        );

        // 6. Esportazione della CDF
        Path output = Path.of(
                "python/erlang/fork_join_erlang_retry_cdf.csv"
        );

        try {
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

            System.out.println("CDF esportata in: " + output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
