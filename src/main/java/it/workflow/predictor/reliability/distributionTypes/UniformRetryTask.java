//package it.workflow.predictor.reliability.distributionTypes;
//
//import java.math.BigDecimal;
//import java.util.Objects;
//
//
//import it.workflow.predictor.reliability.RetryConfig;
//import it.workflow.predictor.reliability.RetryModel;
//import org.oristool.eulero.modeling.Simple;
//
///** Dati minimi per una Simple uniforme di Eulero con una politica di retry. */
//public final class UniformRetryTask implements RetryTask {
//    private final String name;
//    private final BigDecimal min;
//    private final BigDecimal max;
//    private final RetryConfig retry;
//
//    public UniformRetryTask (String name, BigDecimal min, BigDecimal max, RetryConfig retry) {
//        if (name == null || name.trim().isEmpty()) {
//            throw new IllegalArgumentException("Nome del task obbligatorio");
//        }
//        Objects.requireNonNull(min, "min");
//        Objects.requireNonNull(max, "max");
//        Objects.requireNonNull(retry, "retry");
//        if (min.signum() < 0 || max.compareTo(min) <= 0) {
//            throw new IllegalArgumentException("Intervallo uniforme non valido");
//        }
//        this.name = name;
//        this.min = min;
//        this.max = max;
//        this.retry = retry;
//    }
//
//    public String name() { return name; }
//    public BigDecimal min() { return min; }
//    public BigDecimal max() { return max; }
//    public RetryConfig retry() { return retry; }
//
//    /** Distribuzione originale del singolo tentativo (Simple di Eulero). */
//    public Simple originalActivity(String suffix) {
//        return Simple.uniform(name + suffix, min, max);
//    }
//
//
//    @Override
//    public double cdf(double time) {
//
//        double a = min.doubleValue();
//        double b = max.doubleValue();
//
//        if (time <= a) return 0.0;
//        if (time >= b) return 1.0;
//
//        return (time - a) / (b - a);
//    }
//
////    @Override
////    public Simple successfulAttempt(String activityName) {
////        return ConditionalDistribution.uniformSuccess(
////                this, activityName
////        );
////
//
//    @Override
//    public Simple successfulAttempt(String activityName) {
//        {
//            if (RetryModel.successProbability(this) <= 0.0) {
//                throw new IllegalArgumentException(
//                        "La distribuzione condizionata non esiste per p=0");
//            }
//            BigDecimal upper = this.retry().timeout().min(this.max());
//            return Simple.uniform(activityName, this.min(), upper);
//        }
//    }
//
//}
