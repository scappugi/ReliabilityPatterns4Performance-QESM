package it.workflow.predictor.workflowTypesExamples.Mix;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.ForkJoinPool;

public class ForkJoinMix {

    private ForkJoinMix() {}

    public static void main(String[] args) {

        // 1. Costruzione del workflow senza retry
        Activity workflow = DAG.forkJoin(
                "WorkflowForkJoinMix",
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
        String timeStep = "0.1";
        String error = "0.0001";

        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(timeBound, timeStep, error);

        // 3. Lettura della CDF a 5.5 secondi
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
                "Valore teorico atteso: 1.000000"
        );

        // 4. Esportazione dell'intera CDF in CSV
        try (PrintWriter writer =
                     new PrintWriter("python/mix/fork_join_mix_no_retry_cdf.csv")) {

            writer.println("time,cdf");

            double step = new BigDecimal(timeStep).doubleValue();
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

            System.out.println(
                    "CDF esportata in fork_join_mix_no_retry_cdf.csv"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
