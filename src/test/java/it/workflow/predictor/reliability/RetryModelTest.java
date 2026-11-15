package it.workflow.predictor.reliability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import it.workflow.predictor.reliability.distributionTypes.UniformRetryTask;
import org.junit.jupiter.api.Test;

class RetryModelTest {
    private static UniformRetryTask t1(int c) {
        return new UniformRetryTask("T1", new BigDecimal("1"), new BigDecimal("2"),
            new RetryConfig(new BigDecimal("1.5"), c));
    }

    @Test void weightsAndEquation4() {
        UniformRetryTask task = t1(2);
        assertEquals(0.5, RetryModel.taskProbability(task, 0), 1e-12);
        assertEquals(0.25, RetryModel.taskProbability(task, 1), 1e-12);
        assertEquals(0.25, RetryModel.taskProbability(task, 2), 1e-12);
        assertEquals(0.5, RetryModel.taskCdfWithRetries(task, 1.5), 1e-12);
        assertEquals(0.75, RetryModel.taskCdfWithRetries(task, 3.0), 1e-12);
        assertEquals(0.875, RetryModel.taskCdfWithRetries(task, 4.5), 1e-12);
        assertEquals(1.0, RetryModel.taskCdfWithRetries(task, 5.0), 1e-12);
    }

    @Test void noRetryUsesOriginalCdf() {
        UniformRetryTask task = t1(0);
        assertEquals(1.0, RetryModel.taskProbability(task, 0), 1e-12);
        assertEquals(RetryModel.originalCdf(task, 1.6),
            RetryModel.taskCdfWithRetries(task, 1.6), 1e-12);
    }

    @Test void twoTasksHaveSixScenariosAndTotalWeightOne() {
        UniformRetryTask b = new UniformRetryTask("T2", new BigDecimal("2"), new BigDecimal("3"),
            new RetryConfig(new BigDecimal("2.5"), 1));
        List<RetryScenario> scenarios = ScenarioEnumerator.enumerate(Arrays.asList(t1(2), b));
        assertEquals(6, scenarios.size());
        assertEquals(1.0, scenarios.stream().mapToDouble(RetryScenario::weight).sum(), 1e-12);
    }

    @Test void zeroProbabilityConditionalBranchIsSkipped() {
        UniformRetryTask t = new UniformRetryTask("Tzero", new BigDecimal("1"), new BigDecimal("2"),
            new RetryConfig(new BigDecimal("0.5"), 2));
        List<RetryScenario> scenarios = ScenarioEnumerator.enumerate(Arrays.asList(t));
        assertEquals(1, scenarios.size());
        assertEquals(2, scenarios.get(0).retryCount("Tzero"));
        assertEquals(1.0, scenarios.get(0).weight(), 1e-12);
        assertThrows(IllegalArgumentException.class,
            () -> ConditionalDistribution.uniformSuccess(t, "impossible"));
    }
}
