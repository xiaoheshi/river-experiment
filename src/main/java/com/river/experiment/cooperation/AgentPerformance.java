package com.river.experiment.cooperation;

/**
 * 单个角色（代理人）在锦标赛中的表现。
 */
public final class AgentPerformance implements Comparable<AgentPerformance> {

    private final String agentId;
    private final CooperationStrategy strategy;
    private final double totalScore;
    private final int matches;
    private final double totalCooperationRate;
    private final double totalMutualCooperationRate;
    private final double minScore;
    private final double maxScore;
    private final double scoreSquares;

    public AgentPerformance(String agentId,
                            CooperationStrategy strategy,
                            double totalScore,
                            int matches,
                            double totalCooperationRate,
                            double totalMutualCooperationRate,
                            double minScore,
                            double maxScore,
                            double scoreSquares) {
        this.agentId = agentId;
        this.strategy = strategy;
        this.totalScore = totalScore;
        this.matches = matches;
        this.totalCooperationRate = totalCooperationRate;
        this.totalMutualCooperationRate = totalMutualCooperationRate;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.scoreSquares = scoreSquares;
    }

    public String agentId() {
        return agentId;
    }

    public CooperationStrategy strategy() {
        return strategy;
    }

    public double totalScore() {
        return totalScore;
    }

    public int matches() {
        return matches;
    }

    public double meanScore() {
        return matches == 0 ? 0.0 : totalScore / matches;
    }

    public double meanScorePerRound(int roundsPerMatch) {
        if (roundsPerMatch <= 0) {
            return 0.0;
        }
        return meanScore() / roundsPerMatch;
    }

    public double meanCooperationRate() {
        return matches == 0 ? 0.0 : totalCooperationRate / matches;
    }

    public double meanDefectionRate() {
        return 1.0 - meanCooperationRate();
    }

    public double meanMutualCooperationRate() {
        return matches == 0 ? 0.0 : totalMutualCooperationRate / matches;
    }

    public double bestMatchScore() {
        return matches == 0 ? 0.0 : maxScore;
    }

    public double worstMatchScore() {
        return matches == 0 ? 0.0 : minScore;
    }

    public double scoreStdDeviation() {
        if (matches <= 1) {
            return 0.0;
        }
        double mean = meanScore();
        double variance = (scoreSquares / matches) - mean * mean;
        return variance <= 0 ? 0.0 : Math.sqrt(variance);
    }

    @Override
    public int compareTo(AgentPerformance other) {
        return Double.compare(other.meanScore(), this.meanScore());
    }
}
