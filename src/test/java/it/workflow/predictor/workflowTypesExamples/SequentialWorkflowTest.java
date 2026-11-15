package it.workflow.predictor.workflowTypesExamples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.oristool.eulero.modeling.Activity;
import org.oristool.eulero.modeling.DAG;
import org.oristool.eulero.modeling.Simple;

import java.math.BigDecimal;

class SequentialWorkflowTest {
    @Test
    void sequenceHasExpectedSupport() {
        Activity workflow = DAG.sequence("WorkflowSequenziale",
                Simple.uniform("T1", new BigDecimal("1"), new BigDecimal("5")),
                Simple.uniform("T2", new BigDecimal("2"), new BigDecimal("3")));
        assertEquals(3.0, workflow.min().doubleValue(), 1e-9);
        assertEquals(5.0, workflow.max().doubleValue(), 1e-9);
    }
}
