package com.river.experiment.kinselection;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * 某一代的统计指标集合。
 */
public final class GenerationStats {

    private final int generation;
    private final EnumMap<Strategy, StrategySnapshot> strategySnapshots;
    private final int populationSize;

    GenerationStats(int generation, EnumMap<Strategy, StrategySnapshot> strategySnapshots) {
        this.generation = generation;
        this.strategySnapshots = new EnumMap<>(strategySnapshots);
        this.populationSize = strategySnapshots.values().stream()
                .mapToInt(StrategySnapshot::count)
                .sum();
    }

    public int generation() {
        return generation;
    }

    public StrategySnapshot snapshot(Strategy strategy) {
        return strategySnapshots.get(strategy);
    }

    public Map<Strategy, StrategySnapshot> snapshots() {
        return Collections.unmodifiableMap(strategySnapshots);
    }

    public double share(Strategy strategy) {
        StrategySnapshot snapshot = strategySnapshots.get(strategy);
        if (snapshot == null || populationSize == 0) {
            return 0.0;
        }
        return (double) snapshot.count() / populationSize;
    }

    public int populationSize() {
        return populationSize;
    }
}
