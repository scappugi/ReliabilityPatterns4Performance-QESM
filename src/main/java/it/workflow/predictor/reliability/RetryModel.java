
package it.workflow.predictor.reliability;

import it.workflow.predictor.reliability.distributionTypes.RetryTask;

public final class RetryModel {

    private RetryModel() {}

    // CDF originale F_i(x), delegata al task concreto.
    public static double originalCdf(
            RetryTask task, double x) {

        return task.cdf(x);
    }

    // Probabilita' di successo entro il timeout:
    // p_i = F_i(t_i)
    public static double successProbability(RetryTask task) {

        return task.cdf(
                task.retry().timeout().doubleValue() // semplicemente recuperi il timeout da un RetryConfig
        );
    }

    // Equazione (1) del PDF: D_n(T_i)
    public static double taskProbability(
            RetryTask task, int n) {

        int c = task.retry().maxRetries();

        if (n < 0 || n > c) {
            throw new IllegalArgumentException(
                    "Numero di retry non valido"
            );
        }

        double p = successProbability(task);

        if (n < c) {
            return Math.pow(1.0 - p, n) * p;
        }

        return Math.pow(1.0 - p, c);
    }

    // Equazione (4): CDF del task con retry.
    public static double taskCdfWithRetries(
            RetryTask task, double x) {

        double p = successProbability(task);
        double timeout = task.retry().timeout().doubleValue();
        int c = task.retry().maxRetries();

        double result = 0.0;

        for (int n = 0; n < c; n++) {

            double argument = Math.min(
                    x - n * timeout,
                    timeout
            );

            result +=
                    Math.pow(1.0 - p, n)
                            * task.cdf(argument);
        }

        result +=
                Math.pow(1.0 - p, c)
                        * task.cdf(x - c * timeout);

        return result;
    }
}
