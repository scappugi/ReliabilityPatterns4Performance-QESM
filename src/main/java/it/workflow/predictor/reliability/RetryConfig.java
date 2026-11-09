package it.workflow.predictor.reliability;

import java.math.BigDecimal;
import java.util.Objects;

/** Politica di timeout/retry: c retry significano al massimo c+1 tentativi. */
public final class RetryConfig {
    private final BigDecimal timeout;
    private final int maxRetries;

    public RetryConfig(BigDecimal timeout, int maxRetries) {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.signum() <= 0) {
            throw new IllegalArgumentException("Il timeout deve essere positivo");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries non puo' essere negativo");
        }
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    public BigDecimal timeout() { return timeout; }
    public int maxRetries() { return maxRetries; }
}
