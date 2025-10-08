package com.river.experiment.cooperation;

/**
 * 记录一次角色对局的积分与合作指标。
 */
public final class MatchOutcome {

    private final String participantAId;
    private final String participantBId;
    private final CooperationStrategy strategyA;
    private final CooperationStrategy strategyB;
    private final double scoreA;
    private final double scoreB;
    private final double cooperationRateA;
    private final double cooperationRateB;
    private final double mutualCooperationRate;

    public MatchOutcome(String participantAId,
                        String participantBId,
                        CooperationStrategy strategyA,
                        CooperationStrategy strategyB,
                        double scoreA,
                        double scoreB,
                        double cooperationRateA,
                        double cooperationRateB,
                        double mutualCooperationRate) {
        this.participantAId = participantAId;
        this.participantBId = participantBId;
        this.strategyA = strategyA;
        this.strategyB = strategyB;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.cooperationRateA = cooperationRateA;
        this.cooperationRateB = cooperationRateB;
        this.mutualCooperationRate = mutualCooperationRate;
    }

    public String participantAId() {
        return participantAId;
    }

    public String participantBId() {
        return participantBId;
    }

    public CooperationStrategy strategyA() {
        return strategyA;
    }

    public CooperationStrategy strategyB() {
        return strategyB;
    }

    public double scoreA() {
        return scoreA;
    }

    public double scoreB() {
        return scoreB;
    }

    public double cooperationRateA() {
        return cooperationRateA;
    }

    public double cooperationRateB() {
        return cooperationRateB;
    }

    public double mutualCooperationRate() {
        return mutualCooperationRate;
    }
}
