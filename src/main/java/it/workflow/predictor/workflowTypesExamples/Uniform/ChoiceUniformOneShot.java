package it.workflow.predictor.workflowTypesExamples.Uniform;

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
import it.workflow.predictor.reliability.distributionTypes.UniformRetryTask;

/** Choice Uniform: stesso workflow, task e retry della versione a scenari. */
public final class ChoiceUniformOneShot {
    private ChoiceUniformOneShot() {}

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static void main(String[] args) {
        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // Coerente con ChoiceUniformRetry / ChoiceErlangRetry originali:
        // primo ramo FORK_JOIN(T1,T3,T4), secondo ramo T2.
        RetryTask t1 = new UniformRetryTask("T1", bd("1"), bd("5"), new RetryConfig(bd("3"), 2));
        RetryTask t3 = new UniformRetryTask("T3", bd("2"), bd("7"), new RetryConfig(bd("5"), 2));
        RetryTask t4 = new UniformRetryTask("T4", bd("5"), bd("8"), new RetryConfig(bd("6"), 2));
        RetryTask t2 = new UniformRetryTask("T2", bd("2"), bd("3"), new RetryConfig(bd("2.5"), 1));

        // Un'unica topologia con XOR di retry per ciascun task.
        Activity firstBranch = DAG.forkJoin(
                "FirstBranchUniformOneShot",
                OneShotTaskBuilder.build(t1),
                OneShotTaskBuilder.build(t3),
                OneShotTaskBuilder.build(t4));
        Activity workflow = new XOR(
                "ChoiceUniformOneShot",
                Arrays.asList(firstBranch, OneShotTaskBuilder.build(t2)),
                Arrays.asList(0.5, 0.5));

        // Una sola chiamata a Eulero (niente scenario enumeration/mixer).
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(bound, step, error);

        int index = bd("5.0").divide(bd(step)).intValueExact();
        double[][][] solution = result.getSolution();
        System.out.printf(Locale.US,
                "P(T_workflow <= 5 s) = %.6f%n", solution[index][0][0]);

        // Solita esportazione CSV: time,cdf.
        Path output = Path.of("python/uniform/choice_uniform_one_shot_cdf.csv");
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
