package com.river.experiment.cooperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 执行重复囚徒困境锦标赛，角色随机匹配但参赛次数保持一致。
 */
public final class CooperationTournament {

    private final MatchSettings settings;
    private final List<CooperationStrategy> strategies;
    private final Random random;
    private final int agentsPerStrategy;
    private final int encounterRounds;

    public CooperationTournament(MatchSettings settings,
                                 List<CooperationStrategy> strategies,
                                 long seed,
                                 int agentsPerStrategy,
                                 int encounterRounds) {
        if (agentsPerStrategy <= 0) {
            throw new IllegalArgumentException("每种策略至少需要 1 名角色。");
        }
        if (encounterRounds <= 0) {
            throw new IllegalArgumentException("至少需要 1 轮随机匹配。");
        }
        int totalAgents = agentsPerStrategy * strategies.size();
        if (totalAgents % 2 != 0) {
            throw new IllegalArgumentException("总角色数须为偶数，以便两两配对。");
        }
        this.settings = settings;
        this.strategies = List.copyOf(strategies);
        this.random = new Random(seed);
        this.agentsPerStrategy = agentsPerStrategy;
        this.encounterRounds = encounterRounds;
    }

    public TournamentResult run() {
        List<Participant> participants = createParticipants();
        List<MatchOutcome> outcomes = new ArrayList<>(encounterRounds * participants.size() / 2);

        Map<CooperationStrategy, StrategyAccumulator> strategyAccumulators = new EnumMap<>(CooperationStrategy.class);
        for (CooperationStrategy strategy : strategies) {
            strategyAccumulators.put(strategy, new StrategyAccumulator());
        }

        for (int round = 0; round < encounterRounds; round++) {
            Collections.shuffle(participants, random);
            for (int index = 0; index < participants.size(); index += 2) {
                Participant participantA = participants.get(index);
                Participant participantB = participants.get(index + 1);
                MatchOutcome outcome = playMatch(participantA, participantB);
                outcomes.add(outcome);

                strategyAccumulators.get(participantA.strategy)
                        .add(outcome.scoreA(), outcome.cooperationRateA(), outcome.mutualCooperationRate());
                strategyAccumulators.get(participantB.strategy)
                        .add(outcome.scoreB(), outcome.cooperationRateB(), outcome.mutualCooperationRate());
            }
        }

        List<AgentPerformance> agentPerformances = new ArrayList<>(participants.size());
        for (Participant participant : participants) {
            agentPerformances.add(participant.performance());
        }
        agentPerformances.sort(null);

        List<StrategyPerformance> strategyPerformances = new ArrayList<>(strategies.size());
        for (CooperationStrategy strategy : strategies) {
            StrategyAccumulator accumulator = strategyAccumulators.get(strategy);
            strategyPerformances.add(new StrategyPerformance(
                    strategy,
                    accumulator.totalScore,
                    accumulator.matches,
                    accumulator.totalCooperation,
                    accumulator.totalMutualCooperation,
                    accumulator.minScore,
                    accumulator.maxScore,
                    accumulator.scoreSquares
            ));
        }
        strategyPerformances.sort(null);

        return new TournamentResult(agentPerformances, strategyPerformances, outcomes);
    }

    private List<Participant> createParticipants() {
        List<Participant> participants = new ArrayList<>(strategies.size() * agentsPerStrategy);
        for (CooperationStrategy strategy : strategies) {
            for (int i = 0; i < agentsPerStrategy; i++) {
                String id = strategy.displayName() + "#" + (i + 1);
                participants.add(new Participant(id, strategy));
            }
        }
        return participants;
    }

    private MatchOutcome playMatch(Participant participantA, Participant participantB) {
        List<Action> historyA = new ArrayList<>(settings.rounds());
        List<Action> historyB = new ArrayList<>(settings.rounds());

        double scoreA = 0.0;
        double scoreB = 0.0;
        int cooperationCountA = 0;
        int cooperationCountB = 0;
        int mutualCooperationCount = 0;

        for (int round = 0; round < settings.rounds(); round++) {
            Action actionA = participantA.strategy.decide(round, historyA, historyB, random);
            Action actionB = participantB.strategy.decide(round, historyB, historyA, random);

            actionA = maybeFlip(actionA);
            actionB = maybeFlip(actionB);

            historyA.add(actionA);
            historyB.add(actionB);

            double[] payoffs = payoff(actionA, actionB);
            scoreA += payoffs[0];
            scoreB += payoffs[1];

            if (actionA == Action.COOPERATE) {
                cooperationCountA++;
            }
            if (actionB == Action.COOPERATE) {
                cooperationCountB++;
            }
            if (actionA == Action.COOPERATE && actionB == Action.COOPERATE) {
                mutualCooperationCount++;
            }
        }

        double rounds = settings.rounds();
        double cooperationRateA = cooperationCountA / rounds;
        double cooperationRateB = cooperationCountB / rounds;
        double mutualCooperationRate = mutualCooperationCount / rounds;

        participantA.accumulator.add(scoreA, cooperationRateA, mutualCooperationRate);
        participantB.accumulator.add(scoreB, cooperationRateB, mutualCooperationRate);

        return new MatchOutcome(
                participantA.id,
                participantB.id,
                participantA.strategy,
                participantB.strategy,
                scoreA,
                scoreB,
                cooperationRateA,
                cooperationRateB,
                mutualCooperationRate
        );
    }

    private double[] payoff(Action actionA, Action actionB) {
        if (actionA == Action.COOPERATE && actionB == Action.COOPERATE) {
            return new double[]{settings.reward(), settings.reward()};
        }
        if (actionA == Action.COOPERATE && actionB == Action.DEFECT) {
            return new double[]{settings.sucker(), settings.temptation()};
        }
        if (actionA == Action.DEFECT && actionB == Action.COOPERATE) {
            return new double[]{settings.temptation(), settings.sucker()};
        }
        return new double[]{settings.punishment(), settings.punishment()};
    }

    private Action maybeFlip(Action action) {
        if (random.nextDouble() < settings.noiseProbability()) {
            return action.opposite();
        }
        return action;
    }

    private static final class Participant {
        private final String id;
        private final CooperationStrategy strategy;
        private final AgentAccumulator accumulator = new AgentAccumulator();

        Participant(String id, CooperationStrategy strategy) {
            this.id = id;
            this.strategy = strategy;
        }

        AgentPerformance performance() {
            return accumulator.toPerformance(id, strategy);
        }
    }

    private static final class AgentAccumulator {
        private double totalScore = 0.0;
        private double scoreSquares = 0.0;
        private double minScore = Double.POSITIVE_INFINITY;
        private double maxScore = Double.NEGATIVE_INFINITY;
        private int matches = 0;
        private double totalCooperation = 0.0;
        private double totalMutualCooperation = 0.0;

        void add(double score, double cooperationRate, double mutualCooperationRate) {
            totalScore += score;
            scoreSquares += score * score;
            minScore = Math.min(minScore, score);
            maxScore = Math.max(maxScore, score);
            matches++;
            totalCooperation += cooperationRate;
            totalMutualCooperation += mutualCooperationRate;
        }

        AgentPerformance toPerformance(String id, CooperationStrategy strategy) {
            return new AgentPerformance(
                    id,
                    strategy,
                    totalScore,
                    matches,
                    totalCooperation,
                    totalMutualCooperation,
                    matches == 0 ? 0.0 : minScore,
                    matches == 0 ? 0.0 : maxScore,
                    scoreSquares
            );
        }
    }

    private static final class StrategyAccumulator {
        private double totalScore = 0.0;
        private double scoreSquares = 0.0;
        private double minScore = Double.POSITIVE_INFINITY;
        private double maxScore = Double.NEGATIVE_INFINITY;
        private int matches = 0;
        private double totalCooperation = 0.0;
        private double totalMutualCooperation = 0.0;

        void add(double score, double cooperationRate, double mutualCooperationRate) {
            totalScore += score;
            scoreSquares += score * score;
            minScore = Math.min(minScore, score);
            maxScore = Math.max(maxScore, score);
            matches++;
            totalCooperation += cooperationRate;
            totalMutualCooperation += mutualCooperationRate;
        }
    }
}
