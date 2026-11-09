package it.workflow.predictor.examples;

import java.math.BigDecimal;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;

/** Modello di prova: T1 ~ Uniforme(1,2), T2 ~ Uniforme(2,3), in sequenza. */
public final class SequentialWorkflow {
    private SequentialWorkflow() {}

    public static Activity create() {
        return DAG.sequence("WorkflowSequenziale",
                Simple.uniform("T1", new BigDecimal("1"), new BigDecimal("2")),
                Simple.uniform("T2", new BigDecimal("2"), new BigDecimal("3")));
    }
}
