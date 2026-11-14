package it.workflow.predictor.examples;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.UniformRetryTask;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.io.PrintWriter;
import java.io.IOException;

/** Esempio successivo: T1 e T2 in sequenza, con 6 scenari complessivi. */
public final class TwoTaskSequentialDemo {
    private TwoTaskSequentialDemo() {}

    public static void main(String[] args) {
        List<UniformRetryTask> tasks = Arrays.asList(
            new UniformRetryTask("T1", new BigDecimal("1"), new BigDecimal("5"),
                new RetryConfig(new BigDecimal("3"), 2)),
            new UniformRetryTask("T2", new BigDecimal("2"), new BigDecimal("3"),
                new RetryConfig(new BigDecimal("2.5"), 1))
        );
        String bound = "20";
        String step = "0.05";
        List<RetryScenario> scenarios = ScenarioEnumerator.enumerate(tasks);
        List<double[]> cdfs = new ArrayList<>();

        System.out.println("Scenari totali: " + scenarios.size());
        for (RetryScenario scenario : scenarios) {
            System.out.println(scenario);
            cdfs.add(ScenarioEvaluator.evaluate(tasks, scenario,
                ScenarioEvaluator.Composition.SEQUENCE, bound, step, "0.0001"));
        }
//        double[] finalCdf = ScenarioMixer.mix(scenarios, cdfs);
//        int index = new BigDecimal("5.0").divide(new BigDecimal(step)).intValueExact();
//        System.out.printf(Locale.US, "P(T_workflow <= 5 s) = %.6f%n", finalCdf[index]);
//
//

        double[] finalCdf = ScenarioMixer.mix(scenarios, cdfs);

        double delta = new BigDecimal(step).doubleValue();

        System.out.println("Tempo (s)\tCDF");

        for (int i = 0; i < finalCdf.length; i++) {
            double t = i * delta;

            System.out.printf(Locale.US, "%.2f\t%.6f%n", t, finalCdf[i]);
        }
        try (PrintWriter writer = new PrintWriter("python/workflow_cdf.csv")) {

            writer.println("time,cdf");

            for (int i = 0; i < finalCdf.length; i++) {
                double t = i * new BigDecimal(step).doubleValue();

                writer.printf(Locale.US, "%.2f,%.8f%n", t, finalCdf[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
