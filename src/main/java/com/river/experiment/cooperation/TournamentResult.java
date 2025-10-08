package com.river.experiment.cooperation;

import java.util.Collections;
import java.util.List;

/**
 * 锦标赛结果，包含角色排名、策略汇总以及原始对局数据。
 */
public final class TournamentResult {

    private final List<AgentPerformance> agentPerformances;
    private final List<StrategyPerformance> strategyPerformances;
    private final List<MatchOutcome> matchOutcomes;

    public TournamentResult(List<AgentPerformance> agentPerformances,
                            List<StrategyPerformance> strategyPerformances,
                            List<MatchOutcome> matchOutcomes) {
        this.agentPerformances = List.copyOf(agentPerformances);
        this.strategyPerformances = List.copyOf(strategyPerformances);
        this.matchOutcomes = List.copyOf(matchOutcomes);
    }

    public List<AgentPerformance> agentPerformances() {
        return Collections.unmodifiableList(agentPerformances);
    }

    public List<StrategyPerformance> strategyPerformances() {
        return Collections.unmodifiableList(strategyPerformances);
    }

    public List<MatchOutcome> matchOutcomes() {
        return Collections.unmodifiableList(matchOutcomes);
    }

    public AgentPerformance topAgent() {
        return agentPerformances.isEmpty() ? null : agentPerformances.get(0);
    }

    public StrategyPerformance topStrategy() {
        return strategyPerformances.isEmpty() ? null : strategyPerformances.get(0);
    }
}
