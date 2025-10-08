package com.river.experiment.kinselection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 核心模拟器，实现基于汉密尔顿框架的亲缘选择动力学。
 */
public final class KinSelectionSimulation {

    private static final Strategy[] STRATEGIES = Strategy.values();

    private final SimulationParameters parameters;
    private final Random random;
    private List<Agent> population;

    public KinSelectionSimulation(SimulationParameters parameters, long seed) {
        this.parameters = parameters;
        this.random = new Random(seed);
        initializePopulation();
    }

    public SimulationResult run() {
        List<GenerationStats> generations = new ArrayList<>(parameters.generations());
        for (int generationIndex = 0; generationIndex < parameters.generations(); generationIndex++) {
            generations.add(runSingleGeneration(generationIndex));
        }
        return new SimulationResult(generations);
    }

    private void initializePopulation() {
        population = new ArrayList<>(parameters.populationSize());
        for (int i = 0; i < parameters.populationSize(); i++) {
            int familyId = i / parameters.familySize();
            Strategy strategy = random.nextDouble() < parameters.initialAltruistShare()
                    ? Strategy.ALTRUIST
                    : Strategy.SELFISH;
            population.add(new Agent(strategy, familyId));
        }
    }

    private GenerationStats runSingleGeneration(int generationIndex) {
        resetAgents();
        List<List<Agent>> families = collectFamilies();
        playKinInteractions(families);
        GenerationStats stats = collectGenerationStats(generationIndex);
        reproduceNextGeneration();
        return stats;
    }

    private void resetAgents() {
        for (Agent agent : population) {
            agent.resetForGeneration(parameters.baseFitness());
        }
    }

    private List<List<Agent>> collectFamilies() {
        List<List<Agent>> families = new ArrayList<>(parameters.familiesPerGeneration());
        for (int i = 0; i < parameters.familiesPerGeneration(); i++) {
            families.add(new ArrayList<>(parameters.familySize()));
        }
        for (Agent agent : population) {
            families.get(agent.familyId()).add(agent);
        }
        return families;
    }

    private void playKinInteractions(List<List<Agent>> families) {
        for (List<Agent> family : families) {
            if (family.size() <= 1) {
                continue;
            }
            double sharePerRelative = parameters.benefit() / (family.size() - 1);
            for (Agent agent : family) {
                if (agent.strategy() != Strategy.ALTRUIST) {
                    continue;
                }
                agent.addDirectFitness(-parameters.cost());
                agent.addBenefitGiven(parameters.benefit());
                for (Agent relative : family) {
                    if (relative == agent) {
                        continue;
                    }
                    relative.addBenefitReceived(sharePerRelative);
                }
            }
        }
    }

    private GenerationStats collectGenerationStats(int generationIndex) {
        EnumMap<Strategy, RunningTotals> totals = new EnumMap<>(Strategy.class);
        for (Strategy strategy : STRATEGIES) {
            totals.put(strategy, new RunningTotals());
        }
        for (Agent agent : population) {
            agent.finalizeFitness(parameters.relatednessWithinFamily());
            RunningTotals totalsForStrategy = totals.get(agent.strategy());
            totalsForStrategy.count++;
            totalsForStrategy.direct += agent.directFitness();
            totalsForStrategy.inclusive += agent.inclusiveFitness();
            totalsForStrategy.given += agent.benefitGiven();
            totalsForStrategy.received += agent.benefitReceived();
        }
        EnumMap<Strategy, StrategySnapshot> snapshots = new EnumMap<>(Strategy.class);
        for (Map.Entry<Strategy, RunningTotals> entry : totals.entrySet()) {
            Strategy strategy = entry.getKey();
            RunningTotals running = entry.getValue();
            if (running.count == 0) {
                snapshots.put(strategy, new StrategySnapshot(strategy, 0, 0, 0, 0, 0));
            } else {
                snapshots.put(strategy, new StrategySnapshot(
                        strategy,
                        running.count,
                        running.direct / running.count,
                        running.inclusive / running.count,
                        running.given / running.count,
                        running.received / running.count
                ));
            }
        }
        return new GenerationStats(generationIndex, snapshots);
    }

    private void reproduceNextGeneration() {
        double totalWeight = population.stream()
                .mapToDouble(this::reproductiveWeight)
                .sum();
        if (totalWeight <= 0) {
            totalWeight = population.size();
        }
        List<Agent> newPopulation = new ArrayList<>(population.size());
        for (int familyId = 0; familyId < parameters.familiesPerGeneration(); familyId++) {
            Strategy parentStrategy = selectParent(totalWeight).strategy();
            for (int memberIndex = 0; memberIndex < parameters.familySize(); memberIndex++) {
                Strategy childStrategy = maybeMutate(parentStrategy);
                newPopulation.add(new Agent(childStrategy, familyId));
            }
        }
        population = newPopulation;
    }

    private Strategy maybeMutate(Strategy strategy) {
        if (random.nextDouble() < parameters.mutationRate()) {
            return STRATEGIES[random.nextInt(STRATEGIES.length)];
        }
        return strategy;
    }

    private Agent selectParent(double totalWeight) {
        double threshold = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (Agent agent : population) {
            cumulative += reproductiveWeight(agent);
            if (cumulative >= threshold) {
                return agent;
            }
        }
        return population.get(population.size() - 1);
    }

    private double reproductiveWeight(Agent agent) {
        double weight = agent.inclusiveFitness();
        return weight > 0 ? weight : 1e-6;
    }

    private static final class RunningTotals {
        int count;
        double direct;
        double inclusive;
        double given;
        double received;
    }
}
