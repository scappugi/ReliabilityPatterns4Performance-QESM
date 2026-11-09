package it.workflow.predictor.reliability;

import java.math.BigDecimal;
import java.util.Objects;
import org.oristool.eulero.modeling.Simple;

/** Dati minimi per una Simple uniforme di Eulero con una politica di retry. */
public final class UniformRetryTask  {
    private final String name;
    private final BigDecimal min;
    private final BigDecimal max;
    private final RetryConfig retry;

    public UniformRetryTask (String name, BigDecimal min, BigDecimal max, RetryConfig retry) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome del task obbligatorio");
        }
        Objects.requireNonNull(min, "min");
        Objects.requireNonNull(max, "max");
        Objects.requireNonNull(retry, "retry");
        if (min.signum() < 0 || max.compareTo(min) <= 0) {
            throw new IllegalArgumentException("Intervallo uniforme non valido");
        }
        this.name = name;
        this.min = min;
        this.max = max;
        this.retry = retry;
    }

    public String name() { return name; }
    public BigDecimal min() { return min; }
    public BigDecimal max() { return max; }
    public RetryConfig retry() { return retry; }

    /** Distribuzione originale del singolo tentativo (Simple di Eulero). */
    public Simple originalActivity(String suffix) {
        return Simple.uniform(name + suffix, min, max);
    }
}
