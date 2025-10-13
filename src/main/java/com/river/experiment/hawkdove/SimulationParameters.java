package com.river.experiment.hawkdove;

/**
 * 鹰鸽博弈的基础参数。
 */
public record SimulationParameters(
        int generations,
        double initialHawkShare,
        double resourceValue,
        double conflictCost,
        double mutationRate,
        double selectionStrength
) {

    public SimulationParameters {
        if (generations <= 0) {
            throw new IllegalArgumentException("generations must be positive");
        }
        if (initialHawkShare <= 0.0 || initialHawkShare >= 1.0) {
            throw new IllegalArgumentException("initialHawkShare must be between 0 and 1 (exclusive)");
        }
        if (resourceValue <= 0.0) {
            throw new IllegalArgumentException("resourceValue must be positive");
        }
        if (conflictCost <= 0.0) {
            throw new IllegalArgumentException("conflictCost must be positive");
        }
        if (mutationRate < 0.0 || mutationRate >= 0.5) {
            throw new IllegalArgumentException("mutationRate must be within [0, 0.5)");
        }
        if (selectionStrength <= 0.0 || selectionStrength > 1.0) {
            throw new IllegalArgumentException("selectionStrength must be within (0, 1]");
        }
    }
}
