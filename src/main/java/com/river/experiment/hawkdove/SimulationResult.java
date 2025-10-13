package com.river.experiment.hawkdove;

import java.util.List;

/**
 * 鹰鸽博弈的模拟结果。
 */
public record SimulationResult(List<GenerationState> generations) {

    public GenerationState firstGeneration() {
        return generations.get(0);
    }

    public GenerationState lastGeneration() {
        return generations.get(generations.size() - 1);
    }
}
