
package it.workflow.predictor.plots.uniform;

import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.Simple;
import org.oristool.eulero.ui.ActivityViewer;

import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trees.DeterministicEnablingState;

import java.math.BigDecimal;
import java.util.List;

public final class UniformTaskViewer {

    private UniformTaskViewer() {}

    public static void main(String[] args) {

        String bound = "10";
        String step = "0.05";
        String error = "0.0001";

        // Distribuzione originale di T1
        Activity t1 = Simple.uniform(
                "T1",
                new BigDecimal("1"),
                new BigDecimal("5")
        );

        // Distribuzione originale di T2
        Activity t2 = Simple.uniform(
                "T2",
                new BigDecimal("2"),
                new BigDecimal("3")
        );

        // Analisi indipendente di T1
        TransientSolution<DeterministicEnablingState, RewardRate> resultT1 =
                t1.analyze(bound, step, error);

        // Analisi indipendente di T2
        TransientSolution<DeterministicEnablingState, RewardRate> resultT2 =
                t2.analyze(bound, step, error);

        // Visualizzazione delle distribuzioni dei singoli task
        ActivityViewer.plot(
                "Distribuzioni originali T1 e T2",
                List.of("T1 - Uniform(1,5)", "T2 - Uniform(2,3)"),
                resultT1,
                resultT2
        );
//        ActivityViewer.plot("Distribuzione T1", List.of("T1"), resultT1);
//
//        ActivityViewer.plot("Distribuzione T2", List.of("T2"), resultT2);
    }
}
