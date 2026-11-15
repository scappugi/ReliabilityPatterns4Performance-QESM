package it.workflow.predictor.reliability;

import it.workflow.predictor.reliability.distributionTypes.RetryTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Enumerazione di tutte le combinazioni n_i in [0, c_i]. */
public final class ScenarioEnumerator {
    private ScenarioEnumerator() {}

    public static List<RetryScenario> enumerate(List<? extends RetryTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("Il workflow deve contenere task");
        }
        Set<String> names = new HashSet<>();
        for (RetryTask task : tasks) {
            if (task == null || !names.add(task.name())) {
                throw new IllegalArgumentException("Task nullo o nomi duplicati");
            }
        }
        List<RetryScenario> result = new ArrayList<>();
                visit(tasks, 0, new LinkedHashMap<>(), 1.0, result);
        return result;
    }

    private static void visit(List<? extends RetryTask> tasks, int index,
                              Map<String, Integer> current, double weight,
                              List<RetryScenario> output) {
        if (index == tasks.size()) {
            output.add(new RetryScenario(current, weight));
            return;
        }
        RetryTask task = tasks.get(index);
        for (int n = 0; n <= task.retry().maxRetries(); n++) {
            double p = RetryModel.taskProbability(task, n);
            if (p == 0.0) continue; // evita rami condizionati impossibili (p=0)
            current.put(task.name(), n);
            visit(tasks, index + 1, current, weight * p, output);
        }
        current.remove(task.name());
    }
}


