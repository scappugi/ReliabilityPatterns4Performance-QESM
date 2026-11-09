package it.workflow.predictor.evaluation;

import it.workflow.predictor.reliability.RetryScenario;
import java.util.List;

/** Step 4 del PDF: F_W(x) = somma_s P(s) * F_(W|s)(x). */
public final class ScenarioMixer {
    private ScenarioMixer() {}

    /** Tutte le CDF devono condividere timeBound, timeStep e istante iniziale. */
    public static double[] mix(List<RetryScenario> scenarios, List<double[]> cdfs) {
        if (scenarios == null || cdfs == null || scenarios.isEmpty()
                || scenarios.size() != cdfs.size()) {
            throw new IllegalArgumentException("Scenari e CDF incompatibili");
        }
        int length = cdfs.get(0).length;
        double[] mixture = new double[length];
        double totalWeight = 0.0;

        for (int s = 0; s < scenarios.size(); s++) {
            double[] cdf = cdfs.get(s);
            double weight = scenarios.get(s).weight();
            if (cdf == null || cdf.length != length) {
                throw new IllegalArgumentException("Le CDF non hanno la stessa griglia");
            }
            totalWeight += weight;
            for (int i = 0; i < length; i++) {
                mixture[i] += weight * cdf[i];
            }
        }
        if (Math.abs(totalWeight - 1.0) > 1e-8) {
            throw new IllegalStateException("I pesi degli scenari non sommano a 1: " + totalWeight);
        }
        return mixture;
    }
}
