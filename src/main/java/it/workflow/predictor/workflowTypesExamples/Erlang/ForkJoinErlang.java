package it.workflow.predictor.workflowTypesExamples.Erlang;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.Locale;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/** T1 ~ Erlang(2,1), T2 ~ Erlang(3,1.5), forkJoin senza retry. */
public final class ForkJoinErlang {

    private ForkJoinErlang() {}

    public static void main(String[] args) {

        // 1. Costruzione del workflow originale senza retry.
        Activity workflow = DAG.forkJoin(
                "WorkflowForkJoinErlang",
                Simple.erlang("T1", 2, new BigDecimal("1")),
                Simple.erlang("T2", 3, new BigDecimal("1.5"))
        );

        System.out.println("Workflow: " + workflow.name());
        // Le Erlang hanno supporto teorico illimitato, non [min,max] finito.

        // 2. Analisi con Eulero.
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

        // 3. Probabilita' di completamento entro 5 secondi.
        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(timeStep)).intValueExact();
        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n", finalCdf[index]);

        // Esportazione della CDF per Python.
        try {
            Path output = Path.of("python/erlang/fork_join_erlang_no_retry_cdf.csv");
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
