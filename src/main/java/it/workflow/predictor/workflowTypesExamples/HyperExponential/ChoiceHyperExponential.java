
package it.workflow.predictor.workflowTypesExamples.HyperExponential;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.stochastictime.HyperExponentialTime;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Choice senza retry, T1 e T2 iperesponenziali. */
public final class ChoiceHyperExponential {

    private ChoiceHyperExponential() {}

    public static void main(String[] args) {

        Activity t1 = new Simple("T1",
                new HyperExponentialTime(
                        new BigDecimal("2"),
                        new BigDecimal("0.1"),
                        new BigDecimal("0.8")
                ));

        Activity t2 = new Simple("T2",
                new HyperExponentialTime(
                        new BigDecimal("1.5"),
                        new BigDecimal("0.15"),
                        new BigDecimal("0.8")
                ));

        Activity workflow = ModelFactory.XOR(
                Arrays.asList(0.5, 0.5),
                t1,
                t2
        );

        String timeBound = "20";
        String timeStep = "0.05";
        String error = "0.0001";

        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(timeBound, timeStep, error);

        double[][][] solution = result.getSolution();

        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(timeStep))
                .intValueExact();

        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                solution[index][0][0]);

        double[] finalCdf = new double[solution.length];

        for (int i = 0; i < solution.length; i++) {
            finalCdf[i] = solution[i][0][0];
        }

        try {
            Path output = Path.of(
                    "python/hyperexponential/choice_hyperexponential_no_retry_cdf.csv"
            );

            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {

                writer.println("time,cdf");

                double stepValue =
                        new BigDecimal(timeStep).doubleValue();

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
