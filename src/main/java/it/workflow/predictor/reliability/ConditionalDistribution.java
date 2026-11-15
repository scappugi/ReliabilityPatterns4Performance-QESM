package it.workflow.predictor.reliability;

import java.math.BigDecimal;

import it.workflow.predictor.reliability.distributionTypes.UniformRetryTask;
import org.oristool.eulero.modeling.Simple;

/** Eq. (2): per R~U(a,b), (R | R<=t) e' U(a,min(b,t)), se p>0. */
public final class ConditionalDistribution {
    private ConditionalDistribution() {}

    public static Simple uniformSuccess(UniformRetryTask task, String activityName) {
        if (RetryModel.successProbability(task) <= 0.0) {
            throw new IllegalArgumentException(
                "La distribuzione condizionata non esiste per p=0");
        }
        BigDecimal upper = task.retry().timeout().min(task.max());
        return Simple.uniform(activityName, task.min(), upper);
    }
}
