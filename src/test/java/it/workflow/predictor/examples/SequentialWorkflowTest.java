package it.workflow.predictor.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.oristool.eulero.modeling.Activity;

class SequentialWorkflowTest {
    @Test
    void sequenceHasExpectedSupport() {
        Activity workflow = SequentialWorkflow.create();
        assertEquals(3.0, workflow.min().doubleValue(), 1e-9);
        assertEquals(5.0, workflow.max().doubleValue(), 1e-9);
    }
}
