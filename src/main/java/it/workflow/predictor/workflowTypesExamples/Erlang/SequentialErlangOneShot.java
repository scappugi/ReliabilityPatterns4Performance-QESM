
package it.workflow.predictor.workflowTypesExamples.Erlang;

import it.workflow.predictor.evaluation.OneShotTaskBuilder;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.ModelFactory;
import org.oristool.eulero.modeling.stochastictime.ErlangTime;

import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Sequential Erlang One-Shot.
 *
 * T1 ~ Erlang(2, 1), timeout=3, maxRetries=2
 * T2 ~ Erlang(3, 1.5), timeout=2.5, maxRetries=1
 */
public final class SequentialErlangOneShot {

    private SequentialErlangOneShot() {}

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static void main(String[] args) {

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // 1. Definizione dei task con RetryTask e StochasticTime
        RetryTask t1 = new RetryTask(
                "T1",
                new ErlangTime(2, 1.0),
                new RetryConfig(bd("3"), 2)
        );

        RetryTask t2 = new RetryTask(
                "T2",
                new ErlangTime(3, 1.5),
                new RetryConfig(bd("2.5"), 1)
        );

        // 2. Sequenza dei due task con retry One-Shot
        Activity workflow = ModelFactory.sequence(
                OneShotTaskBuilder.build(t1),
                OneShotTaskBuilder.build(t2)
        );

        System.out.println("Workflow: " + workflow.name());

        // 3. Una sola analisi Eulero
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(bound, step, error);

        double[][][] solution = result.getSolution();

        // 4. Probabilita' di completamento entro 5 secondi
        int index = bd("5.0")
                .divide(bd(step))
                .intValueExact();

        System.out.printf(
                Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n",
                solution[index][0][0]
        );

        // 5. Esportazione della CDF
        Path output = Path.of(
                "python/erlang/sequence_erlang_one_shot_cdf.csv"
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

                if (writer.checkError()) {
                    throw new IOException(
                            "Errore nella scrittura del CSV"
                    );
                }
            }

            System.out.println("CDF esportata in: " + output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
