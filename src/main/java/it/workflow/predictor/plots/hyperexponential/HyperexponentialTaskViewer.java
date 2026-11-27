
package it.workflow.predictor.plots.hyperexponential;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.HyperExponentialTime;
import org.oristool.eulero.ui.ActivityViewer;

import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.List;

public final class HyperexponentialTaskViewer {

    private HyperexponentialTaskViewer() {}

    public static void main(String[] args) {

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // T1 = 0.8 * Exp(2) + 0.2 * Exp(0.1)
        Activity t1 = new Simple("T1", new HyperExponentialTime(
                new BigDecimal("2"),
                new BigDecimal("0.1"),
                new BigDecimal("0.8")
        ));

        // T2 = 0.8 * Exp(1.5) + 0.2 * Exp(0.15)
        Activity t2 = new Simple("T2", new HyperExponentialTime(
                new BigDecimal("1.5"),
                new BigDecimal("0.15"),
                new BigDecimal("0.8")
        ));

        // Analisi indipendente dei due task
        TransientSolution<DeterministicEnablingState, RewardRate> resultT1 =
                t1.analyze(bound, step, error);

        TransientSolution<DeterministicEnablingState, RewardRate> resultT2 =
                t2.analyze(bound, step, error);

        // Visualizzazione delle PDF e CDF di T1 e T2
        ActivityViewer.plot(
                "Distribuzioni Iperesponenziali T1 e T2",
                List.of("T1 - HyperExp(2, 0.1)", "T2 - HyperExp(1.5, 0.15)"),
                resultT1,
                resultT2
        );
    }
}
