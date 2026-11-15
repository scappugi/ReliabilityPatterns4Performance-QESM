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

/**
 * Choice con retry, stessa struttura di ChoiceUniformRetry.java caricato:
 * - 50%: fork/join di T1, T3 e T4
 * - 50%: solo T2
 */
public final class ChoiceErlangRetry {

    private ChoiceErlangRetry() {}

    public static void main(String[] args) {

        List<RetryTask> tasksT1 = Arrays.asList(
                new ErlangRetryTask("T1", 2, new BigDecimal("1"),
                        new RetryConfig(new BigDecimal("3"), 2)),
                new ErlangRetryTask("T3", 2, new BigDecimal("0.5"),
                        new RetryConfig(new BigDecimal("5"), 2)),
                new ErlangRetryTask("T4", 3, new BigDecimal("0.5"),
                        new RetryConfig(new BigDecimal("6"), 2))
        );

        List<RetryTask> tasksT2 = Arrays.asList(
                new ErlangRetryTask("T2", 3, new BigDecimal("1.5"),
                        new RetryConfig(new BigDecimal("2.5"), 1))
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // Scenari del primo ramo: fork/join di T1, T3, T4.
        List<RetryScenario> scenariosT1 =
                ScenarioEnumerator.enumerate(tasksT1);
        List<double[]> cdfsT1 = new ArrayList<>();

        System.out.println("Scenari del ramo 1: " + scenariosT1.size());
        for (RetryScenario scenario : scenariosT1) {
            System.out.println(scenario);
            cdfsT1.add(ScenarioEvaluator.evaluate(
                    tasksT1, scenario,
                    ScenarioEvaluator.Composition.FORK_JOIN,
                    bound, step, error));
        }
        double[] finalCdfT1 = ScenarioMixer.mix(scenariosT1, cdfsT1);

        // Scenari del secondo ramo: soltanto T2.
        List<RetryScenario> scenariosT2 =
                ScenarioEnumerator.enumerate(tasksT2);
        List<double[]> cdfsT2 = new ArrayList<>();

        System.out.println("Scenari del ramo 2: " + scenariosT2.size());
        for (RetryScenario scenario : scenariosT2) {
            System.out.println(scenario);
            cdfsT2.add(ScenarioEvaluator.evaluate(
                    tasksT2, scenario,
                    ScenarioEvaluator.Composition.SEQUENCE,
                    bound, step, error));
        }
        double[] finalCdfT2 = ScenarioMixer.mix(scenariosT2, cdfsT2);

        // Choice 50%-50% fra i due rami.
        double probabilityT1 = 0.5;
        double probabilityT2 = 0.5;
        if (Math.abs(probabilityT1 + probabilityT2 - 1.0) > 1e-8) {
            throw new IllegalArgumentException("Le probabilita' dei rami non sommano a 1");
        }
        if (finalCdfT1.length != finalCdfT2.length) {
            throw new IllegalArgumentException("Le griglie temporali non coincidono");
        }
        double[] finalCdf = new double[finalCdfT1.length];
        for (int i = 0; i < finalCdf.length; i++) {
            finalCdf[i] = probabilityT1 * finalCdfT1[i]
                    + probabilityT2 * finalCdfT2[i];
        }

        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step)).intValueExact();
        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n", finalCdf[index]);

        // Esportazione della CDF per Python.
        try {
            Path output = Path.of("python/erlang/choice_erlang_retry_cdf.csv");
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
