
package it.workflow.predictor.workflowTypesExamples.Exponential;

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
 * Sequential senza retry.
 * T1 ~ Exp(0.5)
 * T2 ~ Exp(0.8)
 */
public final class SequentialExponential {

    private SequentialExponential() {}

    public static void main(String[] args) {

        // 1. Costruzione del workflow senza retry
        Activity workflow = DAG.sequence(
                "WorkflowSequentialExponential",

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

        // Formula esatta per la sequenza di due esponenziali
        double lambda1 = 0.5;
        double lambda2 = 0.8;
        double time = 5.0;

        double theoretical =
                1.0 -
                        (lambda2 * Math.exp(-lambda1 * time)
                                - lambda1 * Math.exp(-lambda2 * time))
                                / (lambda2 - lambda1);

        System.out.printf(
                Locale.US,
                "Valore teorico atteso: %.6f%n",
                theoretical
        );

        // 4. Esportazione dell'intera CDF
        try {
            Path output = Path.of(
                    "python/exponential/sequence_exponential_no_retry_cdf.csv"
            );

            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {

                writer.println("time,cdf");

                double step =
                        new BigDecimal(timeStep).doubleValue();

                double[][][] solution = result.getSolution();

                for (int i = 0; i < solution.length; i++) {

                    double timePoint = i * step;
                    double cdf = solution[i][0][0];

                    writer.printf(
                            Locale.US,
                            "%.5f,%.8f%n",
                            timePoint,
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
