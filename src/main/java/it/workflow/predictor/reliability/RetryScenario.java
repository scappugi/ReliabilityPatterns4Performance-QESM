package it.workflow.predictor.reliability;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Uno scenario e' una scelta del numero di retry per ogni task. */
public final class RetryScenario {
    private final Map<String, Integer> retryCounts;
    private final double weight;

    public RetryScenario(Map<String, Integer> retryCounts, double weight) {
        if (!Double.isFinite(weight) || weight < 0.0 || weight > 1.0) {
            throw new IllegalArgumentException("Peso non valido");
        }
        this.retryCounts = Collections.unmodifiableMap(new LinkedHashMap<>(retryCounts));
        this.weight = weight;
    }

    public int retryCount(String name) {
        Integer n = retryCounts.get(name);
        if (n == null) throw new IllegalArgumentException("Task non presente: " + name);
        return n;
    }

    public double weight() { return weight; }
    public Map<String, Integer> retryCounts() { return retryCounts; }

    @Override public String toString() {
        return retryCounts + " | peso=" + weight;
    }
}
