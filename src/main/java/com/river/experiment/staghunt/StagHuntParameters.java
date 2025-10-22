package com.river.experiment.staghunt;

/**
 * 鹿猎博弈带信号机制的关键参数。
 */
public record StagHuntParameters(
        int generations,
        int interactionsPerGeneration,
        double selectionStrength,
        double mutationRate,
        double stagPayoff,
        double harePayoff,
        double failedStagPayoff,
        double signalCost,
        double initialSignalerShare,
        double initialFollowerShare,
        double initialLonerShare,
        long seed
) {

    private static final double SHARE_TOLERANCE = 1.0e-6;

    public StagHuntParameters {
        if (generations <= 0) {
            throw new IllegalArgumentException("generations must be positive");
        }
        if (interactionsPerGeneration <= 0) {
            throw new IllegalArgumentException("interactionsPerGeneration must be positive");
        }
        if (selectionStrength <= 0.0 || selectionStrength > 1.0) {
            throw new IllegalArgumentException("selectionStrength must be within (0, 1]");
        }
        if (mutationRate < 0.0 || mutationRate >= 0.5) {
            throw new IllegalArgumentException("mutationRate must be within [0, 0.5)");
        }
        if (stagPayoff <= harePayoff) {
            throw new IllegalArgumentException("stagPayoff must be greater than harePayoff");
        }
        if (harePayoff <= 0.0) {
            throw new IllegalArgumentException("harePayoff must be positive");
        }
        if (failedStagPayoff > harePayoff) {
            throw new IllegalArgumentException("failedStagPayoff should not exceed harePayoff");
        }
        if (signalCost < 0.0) {
            throw new IllegalArgumentException("signalCost must be non-negative");
        }
        if (initialSignalerShare < 0.0 || initialFollowerShare < 0.0 || initialLonerShare < 0.0) {
            throw new IllegalArgumentException("initial shares must be non-negative");
        }
        double shareSum = initialSignalerShare + initialFollowerShare + initialLonerShare;
        if (Math.abs(shareSum - 1.0) > SHARE_TOLERANCE) {
            throw new IllegalArgumentException("initial shares must sum to 1");
        }
    }
}
