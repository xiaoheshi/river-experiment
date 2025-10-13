package com.river.experiment.publicgoods;

/**
 * 公共物品博弈的关键参数。
 */
public record PublicGoodsParameters(
        int generations,
        int groupSize,
        int interactionsPerGeneration,
        double multiplier,
        double contributionCost,
        double lonerPayoff,
        double mutationRate,
        double selectionStrength,
        double initialCooperatorShare,
        double initialDefectorShare,
        long seed
) {

    public PublicGoodsParameters {
        if (generations <= 0) {
            throw new IllegalArgumentException("generations must be positive");
        }
        if (groupSize < 2) {
            throw new IllegalArgumentException("groupSize must be at least 2");
        }
        if (interactionsPerGeneration <= 0) {
            throw new IllegalArgumentException("interactionsPerGeneration must be positive");
        }
        if (multiplier <= 1.0) {
            throw new IllegalArgumentException("multiplier must be greater than 1 to generate a public goods return");
        }
        if (contributionCost <= 0.0) {
            throw new IllegalArgumentException("contributionCost must be positive");
        }
        if (lonerPayoff < 0.0) {
            throw new IllegalArgumentException("lonerPayoff must be non-negative");
        }
        if (mutationRate < 0.0 || mutationRate >= 0.5) {
            throw new IllegalArgumentException("mutationRate must be within [0, 0.5)");
        }
        if (selectionStrength <= 0.0 || selectionStrength > 1.0) {
            throw new IllegalArgumentException("selectionStrength must be within (0, 1]");
        }
        if (initialCooperatorShare < 0.0 || initialDefectorShare < 0.0) {
            throw new IllegalArgumentException("initial shares must be non-negative");
        }
        double initialLonerShare = 1.0 - initialCooperatorShare - initialDefectorShare;
        if (initialLonerShare < 0.0) {
            throw new IllegalArgumentException("initial shares must sum to at most 1");
        }
    }

    public double initialLonerShare() {
        return Math.max(0.0, 1.0 - initialCooperatorShare - initialDefectorShare);
    }
}
