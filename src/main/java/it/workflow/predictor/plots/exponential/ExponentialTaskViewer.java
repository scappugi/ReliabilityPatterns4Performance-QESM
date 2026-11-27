
package it.workflow.predictor.plots.exponential;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.ExponentialTime;
import org.oristool.eulero.ui.ActivityViewer;

import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.List;

public final class ExponentialTaskViewer {

    private ExponentialTaskViewer() {}

    public static void main(String[] args) {

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // T1 ~ Exp(0.5)
        Activity t1 = new Simple(
                "T1",
                new ExponentialTime(new BigDecimal("0.5"))
        );

        // T2 ~ Exp(0.8)
        Activity t2 = new Simple(
                "T2",
                new ExponentialTime(new BigDecimal("0.8"))
        );

        // Analisi indipendente dei due task
        TransientSolution<DeterministicEnablingState, RewardRate> resultT1 =
                t1.analyze(bound, step, error);

        TransientSolution<DeterministicEnablingState, RewardRate> resultT2 =
                t2.analyze(bound, step, error);

        // Visualizzazione delle PDF e CDF di T1 e T2
        ActivityViewer.plot(
                "Distribuzioni Esponenziali T1 e T2",
                List.of("T1 - Exp(0.5)", "T2 - Exp(0.8)"),
                resultT1,
                resultT2
        );
    }
}
