package it.workflow.predictor.workflowTypesExamples.Exponential;

import it.workflow.predictor.evaluation.OneShotTaskBuilder;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.distributionTypes.RetryTask;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.XOR;
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
import it.workflow.predictor.reliability.distributionTypes.ExponentialRetryTask;

/** Choice Exponential: stesso workflow, task e retry della versione a scenari. */
public final class ChoiceExponentialOneShot {
    private ChoiceExponentialOneShot() {}

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static void main(String[] args) {
        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        RetryTask t1 = new ExponentialRetryTask("T1", bd("0.5"), new RetryConfig(bd("3"), 2));
        RetryTask t2 = new ExponentialRetryTask("T2", bd("0.8"), new RetryConfig(bd("2.5"), 1));

        // Un'unica topologia con XOR di retry per ciascun task.
        // La XOR esterna sceglie il ramo; le XOR interne gestiscono i retry.
        Activity workflow = new XOR(
                "ChoiceExponentialOneShot",
                Arrays.asList(OneShotTaskBuilder.build(t1), OneShotTaskBuilder.build(t2)),
                Arrays.asList(0.5, 0.5));

        // Una sola chiamata a Eulero (niente scenario enumeration/mixer).
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(bound, step, error);

        int index = bd("5.0").divide(bd(step)).intValueExact();
        double[][][] solution = result.getSolution();
        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n", solution[index][0][0]);

        // Solita esportazione CSV: time,cdf.
        Path output = Path.of("python/exponential/choice_exponential_one_shot_cdf.csv");
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
