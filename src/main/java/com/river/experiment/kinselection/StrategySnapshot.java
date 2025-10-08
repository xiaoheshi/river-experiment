package com.river.experiment.kinselection;

public record StrategySnapshot(
        Strategy strategy,
        int count,
        double averageDirectFitness,
        double averageInclusiveFitness,
        double averageBenefitGiven,
        double averageBenefitReceived) {
}
