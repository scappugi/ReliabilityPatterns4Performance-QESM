package it.workflow.predictor.workflowTypesExamples.Uniform;

import it.workflow.predictor.evaluation.OneShotTaskBuilder;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.stochastictime.UniformTime;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/** ForkJoin Uniform: stesso workflow, task e retry della versione a scenari. */
public final class ForkJoinUniformOneShot {
    private ForkJoinUniformOneShot() {}

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static void main(String[] args) {
        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        RetryTask t1 = new RetryTask(
                "T1",
                new UniformTime(bd("1"), bd("5")),
                new RetryConfig(bd("3"), 2)
        );

        // T2 ~ U(2,3), timeout=2.5, massimo 1 retry
        RetryTask t2 = new RetryTask(
                "T2",
                new UniformTime(bd("2"), bd("3")),
                new RetryConfig(bd("2.5"), 1)
        );

        System.out.println("ForkJoinUniformOneShot");
        // Un'unica topologia con XOR di retry per ciascun task.
        Activity workflow = ModelFactory.forkJoin(
                OneShotTaskBuilder.build(t1),
                OneShotTaskBuilder.build(t2));

        // Una sola chiamata a Eulero (niente scenario enumeration/mixer).
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(bound, step, error);

        int index = bd("5.0").divide(bd(step)).intValueExact();
        double[][][] solution = result.getSolution();
        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n", solution[index][0][0]);

        // Solita esportazione CSV: time,cdf.
        Path output = Path.of("python/uniform/fork_join_uniform_one_shot_cdf.csv");
        try {
            Files.createDirectories(output.getParent());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(output))) {
                writer.println("time,cdf");
                double timeStep = bd(step).doubleValue();
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
