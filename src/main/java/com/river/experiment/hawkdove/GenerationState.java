package com.river.experiment.hawkdove;

/**
 * 记录每一代的策略占比与收益。
 */
public record GenerationState(
        int generation,
        double hawkShare,
        double doveShare,
        double hawkPayoff,
        double dovePayoff,
        double averagePayoff
) {
}
