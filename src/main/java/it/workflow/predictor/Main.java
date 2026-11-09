package it.workflow.predictor;

import it.workflow.predictor.evaluation.ScenarioEvaluator;
import it.workflow.predictor.evaluation.ScenarioMixer;
import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.RetryModel;
import it.workflow.predictor.reliability.RetryScenario;
import it.workflow.predictor.reliability.ScenarioEnumerator;
import it.workflow.predictor.reliability.UniformRetryTask;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Prima prova completa: scenario enumeration per un task uniforme con retry. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        UniformRetryTask t1 = new UniformRetryTask(
            "T1", new BigDecimal("1"), new BigDecimal("2"),
            new RetryConfig(new BigDecimal("1.5"), 2));

        // Iniziamo da UN task: cosi' la formula (4) e' un riferimento esatto.
        List<UniformRetryTask> tasks = Collections.singletonList(t1);

        String bound = "6";
        String step = "0.05";
        String error = "0.0001";
        List<RetryScenario> scenarios = ScenarioEnumerator.enumerate(tasks);
        List<double[]> scenarioCdfs = new ArrayList<>();

        System.out.println("Scenari:");
        for (RetryScenario scenario : scenarios) {
            System.out.printf(Locale.US, "  %s%n", scenario);
            scenarioCdfs.add(ScenarioEvaluator.evaluate(
                tasks, scenario, ScenarioEvaluator.Composition.SEQUENCE,
                bound, step, error));
        }
        double[] mixture = ScenarioMixer.mix(scenarios, scenarioCdfs);
        BigDecimal timeStep = new BigDecimal(step);

        System.out.println("\nCDF del task con timeout e retry:");
        for (String timestamp : new String[]{"1.5", "3.0", "4.5", "5.0"}) {
            int index = new BigDecimal(timestamp).divide(timeStep).intValueExact();
            double fromEulero = mixture[index];
            double exact = RetryModel.taskCdfWithRetries(t1, Double.parseDouble(timestamp));
            System.out.printf(Locale.US,
                "t=%4s s | Eulero+mix=%.6f | Eq.(4)=%.6f | errore=%.6f%n",
                timestamp, fromEulero, exact, Math.abs(fromEulero - exact));
        }
    }
}
