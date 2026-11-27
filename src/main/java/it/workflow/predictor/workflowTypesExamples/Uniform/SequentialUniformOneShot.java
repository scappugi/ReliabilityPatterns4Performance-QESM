
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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public final class SequentialUniformOneShot {

    private SequentialUniformOneShot() {}

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static void main(String[] args) {

        // T1 ~ U(1,5), timeout=3, massimo 2 retry
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

        // Costruzione del workflow con Eulero 2.0
        Activity workflow = ModelFactory.sequence(
                OneShotTaskBuilder.build(t1),
                OneShotTaskBuilder.build(t2)
        );

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // Calcolo della CDF del workflow
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(bound, step, error);

        double[][][] solution = result.getSolution();

        int index = bd("5.0")
                .divide(bd(step))
                .intValueExact();

        System.out.printf(
                Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                solution[index][0][0]
        );

        // Esportazione CSV
        Path output = Path.of(
                "python/uniform/sequence_uniform_one_shot_cdf.csv"
        );

        try {
            Files.createDirectories(output.getParent());

            try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(output))) {

                writer.println("time,cdf");

                double timeStep = bd(step).doubleValue();

                for (int i = 0; i < solution.length; i++) {
                    writer.printf(
                            Locale.US,
                            "%.5f,%.8f%n",
                            i * timeStep,
                            solution[i][0][0]
                    );
                }
            }

            System.out.println("CDF esportata in: " + output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
