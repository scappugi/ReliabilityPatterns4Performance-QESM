package it.workflow.predictor.workflowTypesExamples.Exponential;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.XOR;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Workflow Choice:
 * T1 ~ Exp(0.5)
 * T2 ~ Exp(0.8)
 *
 * Viene eseguito uno solo dei due task,
 * con probabilità 0.5 per ciascuno.
 */
public final class ChoiceExponential {

    private ChoiceExponential() {}

    public static void main(String[] args) {

        // 1. Creazione dei due task
        Activity task1 = Simple.exp(
                "T1",
                new BigDecimal("0.5")
        );

        Activity task2 = Simple.exp(
                "T2",
                new BigDecimal("0.8")
        );

        // 2. Costruzione del workflow Choice (XOR)
        Activity workflow = new XOR(
                "WorkflowChoiceExponential",
                Arrays.asList(task1, task2),
                Arrays.asList(0.5, 0.5)
        );

        System.out.println("Workflow: " + workflow.name());

        System.out.println(
                "Supporto teorico: [" +
                        workflow.min() + ", " +
                        workflow.max() + "] s"
        );

        // 3. Analisi con Eulero
        String timeBound = "20";
        String timeStep = "0.05";
        String error = "0.0001";

        TransientSolution<DeterministicEnablingState, RewardRate> result =
                workflow.analyze(timeBound, timeStep, error);

        // 4. Probabilità di completamento entro 3 secondi
        int index = new BigDecimal("3.0")
                .divide(new BigDecimal(timeStep))
                .intValueExact();

        double probability = result.getSolution()[index][0][0];

        System.out.printf(
                Locale.US,
                "CDF stimata da Eulero: P(T <= 3 s) = %.6f%n",
                probability
        );

        // 5. Esportazione dell'intera CDF
        try (PrintWriter writer =
                     new PrintWriter("python/exponential/choice_exponential_no_retry_cdf.csv")) {

            writer.println("time,cdf");

            double step = new BigDecimal(timeStep).doubleValue();
            double[][][] solution = result.getSolution();

            for (int i = 0; i < solution.length; i++) {

                double time = i * step;
                double cdf = solution[i][0][0];

                writer.printf(
                        Locale.US,
                        "%.5f,%.8f%n",
                        time,
                        cdf
                );
            }

            System.out.println(
                    "CDF esportata in choice_exponential_no_retry_cdf.csv"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}