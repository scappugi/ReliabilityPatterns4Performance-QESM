
package it.workflow.predictor.examples;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Fork/Join senza retry.
 * T1 ~ Exp(0.5)
 * T2 ~ Exp(0.8)
 */
public final class ForkJoinExponentialWorkflow {

    private ForkJoinExponentialWorkflow() {}

    public static void main(String[] args) {

        // 1. Costruzione del workflow senza retry
        Activity workflow = DAG.forkJoin(
                "WorkflowForkJoinExponential",

                Simple.exp(
                        "T1",
                        new BigDecimal("0.5")
                ),

                Simple.exp(
                        "T2",
                        new BigDecimal("0.8")
                )
        );

        System.out.println("Workflow: " + workflow.name());

        // 2. Analisi con Eulero
        String timeBound = "20";
        String timeStep = "0.05";
        String error = "0.0001";

        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(timeBound, timeStep, error);

        // 3. Probabilita' entro 5 secondi
        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(timeStep))
                .intValueExact();

        double probability =
                result.getSolution()[index][0][0];

        System.out.printf(
                Locale.US,
                "CDF stimata da Eulero: P(T <= 5 s) = %.6f%n",
                probability
        );

        // Formula esatta per il fork/join di due esponenziali
        double theoretical =
                (1.0 - Math.exp(-0.5 * 5.0)) *
                        (1.0 - Math.exp(-0.8 * 5.0));

        System.out.printf(
                Locale.US,
                "Valore teorico atteso: %.6f%n",
                theoretical
        );

        // 4. Esportazione dell'intera CDF
        try {
            Path output = Path.of(
                    "python/fork_join_exponential_no_retry_cdf.csv"
            );

            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {

                writer.println("time,cdf");

                double step =
                        new BigDecimal(timeStep).doubleValue();

                double[][][] solution = result.getSolution();

                for (int i = 0; i < solution.length; i++) {

                    double time = i * step;
                    double cdf = solution[i][0][0];

                    writer.printf(
                            Locale.US,
                            "%.5f,%.8f%n",
                            time,
                            cdf
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
