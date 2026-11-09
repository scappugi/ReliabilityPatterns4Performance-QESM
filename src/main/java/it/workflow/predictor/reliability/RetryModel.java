package it.workflow.predictor.reliability;

/** Equazioni (1) e (4) del PDF, specializzate a un tempo originale uniforme. */
public final class RetryModel {
    private RetryModel() {}

    // F_i(x) per R_i ~ U(a,b)
    public static double originalCdf(UniformRetryTask task, double x) {
        double a = task.min().doubleValue();
        double b = task.max().doubleValue();
        if (x <= a) return 0.0;
        if (x >= b) return 1.0;
        return (x - a) / (b - a);
    }

    // p_i = F_i(t_i)
    public static double successProbability(UniformRetryTask task) {
        return originalCdf(task, task.retry().timeout().doubleValue());
    }

    // Eq. (1): D_n = (1-p)^n p, n<c; D_c=(1-p)^c
    public static double taskProbability(UniformRetryTask task, int n) {
        int c = task.retry().maxRetries();
        if (n < 0 || n > c) {
            throw new IllegalArgumentException("Numero di retry fuori intervallo");
        }
        double p = successProbability(task);
        return n < c ? Math.pow(1.0 - p, n) * p : Math.pow(1.0 - p, c);
    }

    /** Eq. (4): CDF teorica del task completo con timeout e retry. */
    public static double taskCdfWithRetries(UniformRetryTask task, double x) {
        double p = successProbability(task);
        double t = task.retry().timeout().doubleValue();
        int c = task.retry().maxRetries();
        double sum = 0.0;
        for (int n = 0; n < c; n++) {
            double argument = Math.min(x - n * t, t);
            sum += Math.pow(1.0 - p, n) * originalCdf(task, argument);
        }
        sum += Math.pow(1.0 - p, c) * originalCdf(task, x - c * t);
        return sum;
    }
}
