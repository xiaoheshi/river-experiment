package com.river.experiment.hawkdove;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于复制器动力学的鹰鸽博弈模拟。
 */
public final class HawkDoveSimulation {

    private static final double EPSILON = 1.0e-8;

    private final SimulationParameters parameters;

    public HawkDoveSimulation(SimulationParameters parameters) {
        this.parameters = parameters;
    }

    public SimulationResult run() {
        List<GenerationState> history = new ArrayList<>();
        double hawkShare = parameters.initialHawkShare();
        double doveShare = 1.0 - hawkShare;

        for (int generation = 0; generation <= parameters.generations(); generation++) {
            double hawkPayoff = hawkExpectedPayoff(hawkShare);
            double dovePayoff = doveExpectedPayoff(hawkShare);
            double averagePayoff = hawkShare * hawkPayoff + doveShare * dovePayoff;

            history.add(new GenerationState(
                    generation,
                    hawkShare,
                    doveShare,
                    hawkPayoff,
                    dovePayoff,
                    averagePayoff
            ));

            if (generation == parameters.generations()) {
                break;
            }

            double selectionStrength = parameters.selectionStrength();
            double hawkAdjustment = selectionStrength * hawkShare * (hawkPayoff - averagePayoff);
            double doveAdjustment = selectionStrength * doveShare * (dovePayoff - averagePayoff);

            double nextHawk = hawkShare + hawkAdjustment;
            double nextDove = doveShare + doveAdjustment;

            double normalizedHawk = clamp(nextHawk, EPSILON, 1.0 - EPSILON);
            double normalizedDove = clamp(nextDove, EPSILON, 1.0 - EPSILON);
            double sum = normalizedHawk + normalizedDove;
            normalizedHawk /= sum;
            normalizedDove /= sum;

            double mutationRate = parameters.mutationRate();
            if (mutationRate > 0.0) {
                normalizedHawk = (1.0 - mutationRate) * normalizedHawk + mutationRate * 0.5;
                normalizedDove = (1.0 - mutationRate) * normalizedDove + mutationRate * 0.5;
            }

            hawkShare = normalizedHawk;
            doveShare = normalizedDove;
        }

        return new SimulationResult(List.copyOf(history));
    }

    private double hawkExpectedPayoff(double hawkShare) {
        double hawkVsHawk = (parameters.resourceValue() - parameters.conflictCost()) / 2.0;
        double hawkVsDove = parameters.resourceValue();
        double doveShare = 1.0 - hawkShare;
        return hawkShare * hawkVsHawk + doveShare * hawkVsDove;
    }

    private double doveExpectedPayoff(double hawkShare) {
        double doveVsHawk = 0.0;
        double doveVsDove = parameters.resourceValue() / 2.0;
        double doveShare = 1.0 - hawkShare;
        return hawkShare * doveVsHawk + doveShare * doveVsDove;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
