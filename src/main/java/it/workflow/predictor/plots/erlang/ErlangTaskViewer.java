
package it.workflow.predictor.plots.erlang;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.modeling.stochastictime.ErlangTime;
import org.oristool.eulero.ui.ActivityViewer;

import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.util.List;

public final class ErlangTaskViewer {

    private ErlangTaskViewer() {}

    public static void main(String[] args) {

        String bound = "20";
        String step = "0.05";
        String error = "0.0001";

        // T1 ~ Erlang(2, 1)
        Activity t1 = new Simple(
                "T1",
                new ErlangTime(2, 1.0)
        );

        // T2 ~ Erlang(3, 1.5)
        Activity t2 = new Simple(
                "T2",
                new ErlangTime(3, 1.5)
        );

        // Analisi indipendente dei due task
        TransientSolution<DeterministicEnablingState, RewardRate> resultT1 =
                t1.analyze(bound, step, error);

        TransientSolution<DeterministicEnablingState, RewardRate> resultT2 =
                t2.analyze(bound, step, error);

        // Visualizzazione delle PDF e CDF di T1 e T2
        ActivityViewer.plot(
                "Distribuzioni Erlang T1 e T2",
                List.of("T1 - Erlang(2, 1)", "T2 - Erlang(3, 1.5)"),
                resultT1,
                resultT2
        );
    }
}
