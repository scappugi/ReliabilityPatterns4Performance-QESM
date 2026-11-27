package it.workflow.predictor.workflowTypesExamples.Erlang;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.ErlangTime;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import java.util.Arrays;
import java.math.BigDecimal;
import java.util.Locale;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/** Choice originale: 50% T1 ~ Erlang(2,1), 50% T2 ~ Erlang(3,1.5). */
public final class ChoiceErlang {

    private ChoiceErlang() {}

    public static void main(String[] args) {

        // Struttura IDENTICA al ChoiceUniform.java originale:
        // scelta fra due singoli task (diversa dal ramo fork/join del caso retry).

        Activity task1 = new Simple("T1", new ErlangTime(2, 1.0));
        Activity task2 = new Simple("T2", new ErlangTime(3, 1.5));


        Activity workflow = ModelFactory.XOR(
                Arrays.asList(0.5, 0.5),
                task1,
                task2
                );

        System.out.println("Workflow: " + workflow.name());

        String timeBound = "20";
        String timeStep = "0.05";
        String error = "0.0001";

        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(timeBound, timeStep, error);
        double[][][] solution = result.getSolution();
        double[] finalCdf = new double[solution.length];
        for (int i = 0; i < finalCdf.length; i++) {
            finalCdf[i] = solution[i][0][0];
        }

        int index = new BigDecimal("3.0")
                .divide(new BigDecimal(timeStep)).intValueExact();
        System.out.printf(Locale.US,
                "P(T_workflow <= 3 s) = %.6f%n", finalCdf[index]);

        // Esportazione della CDF per Python.
        try {
            Path output = Path.of("python/erlang/choice_erlang_no_retry_cdf.csv");
            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {
                writer.println("time,cdf");
                double step = new BigDecimal(timeStep).doubleValue();

                for (int i = 0; i < finalCdf.length; i++) {
                    writer.printf(Locale.US, "%.5f,%.8f%n",
                            i * step, finalCdf[i]);
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
