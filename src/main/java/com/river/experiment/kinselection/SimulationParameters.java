package com.river.experiment.kinselection;

/**
 * 模拟所需的不可变参数集合。
 */
public final class SimulationParameters {

    private final int populationSize;
    private final int familySize;
    private final int generations;
    private final double baseFitness;
    private final double benefit;
    private final double cost;
    private final double mutationRate;
    private final double relatednessWithinFamily;
    private final double initialAltruistShare;

    public SimulationParameters(int populationSize,
                                int familySize,
                                int generations,
                                double baseFitness,
                                double benefit,
                                double cost,
                                double mutationRate,
                                double relatednessWithinFamily,
                                double initialAltruistShare) {
        if (populationSize <= 0) {
            throw new IllegalArgumentException("人口规模必须为正数。");
        }
        if (familySize <= 1) {
            throw new IllegalArgumentException("家庭规模至少为 2 才能体现亲缘互动。");
        }
        if (populationSize % familySize != 0) {
            throw new IllegalArgumentException("总人口必须能被家庭规模整除。");
        }
        if (generations <= 0) {
            throw new IllegalArgumentException("模拟代数必须为正数。");
        }
        if (baseFitness <= 0) {
            throw new IllegalArgumentException("基础适合度必须为正数。");
        }
        if (benefit <= 0 || cost <= 0) {
            throw new IllegalArgumentException("收益与成本必须为正数。");
        }
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalArgumentException("突变率需介于 0 与 1 之间。");
        }
        if (relatednessWithinFamily < 0 || relatednessWithinFamily > 1) {
            throw new IllegalArgumentException("相关系数需介于 0 与 1 之间。");
        }
        if (initialAltruistShare < 0 || initialAltruistShare > 1) {
            throw new IllegalArgumentException("初始利他者占比需介于 0 与 1 之间。");
        }
        this.populationSize = populationSize;
        this.familySize = familySize;
        this.generations = generations;
        this.baseFitness = baseFitness;
        this.benefit = benefit;
        this.cost = cost;
        this.mutationRate = mutationRate;
        this.relatednessWithinFamily = relatednessWithinFamily;
        this.initialAltruistShare = initialAltruistShare;
    }

    public int populationSize() {
        return populationSize;
    }

    public int familySize() {
        return familySize;
    }

    public int familiesPerGeneration() {
        return populationSize / familySize;
    }

    public int generations() {
        return generations;
    }

    public double baseFitness() {
        return baseFitness;
    }

    public double benefit() {
        return benefit;
    }

    public double cost() {
        return cost;
    }

    public double mutationRate() {
        return mutationRate;
    }

    public double relatednessWithinFamily() {
        return relatednessWithinFamily;
    }

    public double initialAltruistShare() {
        return initialAltruistShare;
    }
}
