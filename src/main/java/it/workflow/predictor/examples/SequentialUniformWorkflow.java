package it.workflow.predictor.examples;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import it.workflow.predictor.reliability.RetryConfig;
import it.workflow.predictor.reliability.UniformRetryTask;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

/** T1 ~ Uniforme(1,5), T2 ~ Uniforme(2,3), in sequenza. */
public final class SequentialUniformWorkflow {
    private SequentialUniformWorkflow() {}

    public static void main(String[] args) {
        Activity workflow = DAG.sequence("WorkflowSequenziale",
                Simple.uniform("T1", new BigDecimal("1"), new BigDecimal("5")),
                Simple.uniform("T2", new BigDecimal("2"), new BigDecimal("3")));
        System.out.println("Workflow: " + workflow.name());
        System.out.println("Supporto teorico: [" + workflow.min() + ", " + workflow.max() + "] s");

        String timeBound = "9";
        String timeStep = "0.1";
        String error = "0.0001";
        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(timeBound, timeStep, error);

        int indexAtFourSeconds = 55; // 4.0 / 0.1
        double probability = result.getSolution()[indexAtFourSeconds][0][0];
        System.out.printf(Locale.US,
                "CDF stimata da Eulero: P(T <= 5.5 s) = %.4f%n", probability);
        System.out.println("Valore teorico atteso per questa sequenza: 0.5000");
    }

}