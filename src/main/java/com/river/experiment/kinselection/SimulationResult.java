package com.river.experiment.kinselection;

import java.util.Collections;
import java.util.List;

/**
 * 封装模拟结果，包含历代统计信息。
 */
public final class SimulationResult {

    private final List<GenerationStats> generations;

    SimulationResult(List<GenerationStats> generations) {
        this.generations = List.copyOf(generations);
    }

    public List<GenerationStats> generations() {
        return Collections.unmodifiableList(generations);
    }

    public GenerationStats lastGeneration() {
        return generations.get(generations.size() - 1);
    }
}
