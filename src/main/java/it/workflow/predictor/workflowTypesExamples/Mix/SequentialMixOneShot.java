package it.workflow.predictor.workflowTypesExamples.Mix;

import it.workflow.predictor.evaluation.OneShotTaskBuilder;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.stochastictime.ExponentialTime;
import org.oristool.eulero.modeling.stochastictime.UniformTime;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/**
 * Workflow sequence con task di distribuzioni diverse, valutato one-shot.
 * T1 ~ U(1,5), timeout=3, maxRetries=2.
 * T2 ~ Exp(2), timeout=2.5, maxRetries=1.
 * Nessuna enumerazione esplicita degli scenari.
 */
public final class SequentialMixOneShot {

    private SequentialMixOneShot() {}

    public static void main(String[] args) {
        /*RetryTask t1 = new UniformRetryTask(
                "T1", new BigDecimal("1"), new BigDecimal("5"),
                new RetryConfig(new BigDecimal("3"), 2)
        );
        RetryTask t2 = new ExponentialRetryTask(
                "T2", new BigDecimal("2"),
                new RetryConfig(new BigDecimal("2.5"), 1)
        );*/

        RetryTask t1 = new RetryTask(
                "T1",
                new UniformTime(
                        new BigDecimal("1"),
                        new BigDecimal("5")
                ),
                new RetryConfig(new BigDecimal("3"), 2)
        );

        RetryTask t2 = new RetryTask(
                "T2",
                new ExponentialTime(
                        new BigDecimal("2")
                ),
                new RetryConfig(new BigDecimal("2.5"), 1)
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        System.out.println("SequentialMixOneShot");
        // Ogni RetryTask diventa una XOR locale delle sue possibili durate.
        // La topologia esterna (sequence) e' indipendente dal tipo di distribuzione.
        Activity workflow = ModelFactory.sequence(
                OneShotTaskBuilder.build(t1),
                OneShotTaskBuilder.build(t2)
        );

        // Una sola analisi Eulero dell'intero workflow.
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(bound, step, error);

        double[][][] solution = result.getSolution();
        int index = new BigDecimal("5.0")
                .divide(new BigDecimal(step))
                .intValueExact();

        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n", solution[index][0][0]);

        // Stessa griglia, intestazione e formato CSV degli esempi gia' presenti.
        Path output = Path.of("python/mix/sequence_mix_one_shot_cdf.csv");
        try {
            Files.createDirectories(output.getParent());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(output))) {
                writer.println("time,cdf");
                double timeStep = new BigDecimal(step).doubleValue();
                for (int i = 0; i < solution.length; i++) {
                    writer.printf(Locale.US, "%.5f,%.8f%n",
                            i * timeStep, solution[i][0][0]);
                }
                if (writer.checkError()) {
                    throw new IOException("Errore nella scrittura del CSV");
                }
            }
            System.out.println("CDF esportata in: " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
