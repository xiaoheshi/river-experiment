package com.river.experiment.staghunt;

/**
 * 单代模拟的状态快照。
 */
public record StagHuntGeneration(
        int generation,
        double signalerShare,
        double followerShare,
        double lonerShare,
        double signalerPayoff,
        double followerPayoff,
        double lonerPayoff,
        double populationPayoff,
        double stagSuccessRate,
        double signalActivationRate,
        double stagConversionRate
) {
}
