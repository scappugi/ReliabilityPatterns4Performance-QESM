package it.workflow.predictor.workflowTypesExamples.Mix;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Locale;

public class SequentialMix {

    private SequentialMix() {}

    public static void main(String[] args) {

        // 1. Costruzione del workflow senza retry
        System.out.println("WorkflowSequentialMix");
        Activity workflow = ModelFactory.sequence(
                Simple.uniform(
                        "T1",
                        new BigDecimal("1"),
                        new BigDecimal("5")
                ),
                Simple.exp(
                        "T2",
                        new BigDecimal("2")
                )
        );

        System.out.println("Workflow: " + workflow.name());
        System.out.println(
                "Supporto teorico: [" +
                        workflow.min() + ", " + workflow.max() + "] s"
        );

        // 2. Analisi con Eulero
        String timeBound = "20";
        String timeStep = "0.05";
        String error = "0.0001";

        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(timeBound, timeStep, error);

        // 3. Probabilita' di completare entro 5.5 secondi
        int index = new BigDecimal("5.5")
                .divide(new BigDecimal(timeStep))
                .intValueExact();

        double probability = result.getSolution()[index][0][0];

        System.out.printf(
                Locale.US,
                "CDF stimata da Eulero: P(T <= 5.5 s) = %.6f%n",
                probability
        );

        System.out.println(
                "Valore teorico atteso: 0.500000"
        );

        // 4. Esportazione dell'intera CDF
        try (PrintWriter writer =
                     new PrintWriter("python/mix/sequence_mix_no_retry_cdf.csv")) {

            // Intestazione
            writer.println("time,cdf");

            double step = new BigDecimal(timeStep).doubleValue();
            double[][][] solution = result.getSolution();

            for (int i = 0; i < solution.length; i++) {

                // Istante temporale corrispondente all'indice i
                double time = i * step;

                // Valore della CDF all'istante time
                double cdf = solution[i][0][0];

                writer.printf(
                        Locale.US,
                        "%.5f,%.8f%n",
                        time,
                        cdf
                );
            }

            System.out.println(
                    "CDF esportata in sequence_mix_no_retry_cdf.csv"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
