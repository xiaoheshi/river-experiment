package com.river.experiment.publicgoods;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 自愿参与公共物品博弈的蒙特卡洛复制器模拟。
 */
public final class PublicGoodsSimulation {

    private static final double EPSILON = 1.0e-8;

    private final PublicGoodsParameters parameters;

    public PublicGoodsSimulation(PublicGoodsParameters parameters) {
        this.parameters = parameters;
    }

    public PublicGoodsResult run() {
        Random random = new Random(parameters.seed());
        List<PublicGoodsGeneration> history = new ArrayList<>();

        double cooperatorShare = parameters.initialCooperatorShare();
        double defectorShare = parameters.initialDefectorShare();
        double lonerShare = parameters.initialLonerShare();

        for (int generation = 0; generation <= parameters.generations(); generation++) {
            Totals totals = simulateGeneration(random, cooperatorShare, defectorShare, lonerShare);

            history.add(new PublicGoodsGeneration(
                    generation,
                    cooperatorShare,
                    defectorShare,
                    lonerShare,
                    totals.averageCooperatorPayoff,
                    totals.averageDefectorPayoff,
                    totals.averageLonerPayoff,
                    totals.populationPayoff
            ));

            if (generation == parameters.generations()) {
                break;
            }

            double s = parameters.selectionStrength();
            double nextCooperators = cooperatorShare + s * cooperatorShare * (totals.averageCooperatorPayoff - totals.populationPayoff);
            double nextDefectors = defectorShare + s * defectorShare * (totals.averageDefectorPayoff - totals.populationPayoff);
            double nextLoners = lonerShare + s * lonerShare * (totals.averageLonerPayoff - totals.populationPayoff);

            nextCooperators = Math.max(EPSILON, nextCooperators);
            nextDefectors = Math.max(EPSILON, nextDefectors);
            nextLoners = Math.max(EPSILON, nextLoners);

            double sum = nextCooperators + nextDefectors + nextLoners;
            nextCooperators /= sum;
            nextDefectors /= sum;
            nextLoners /= sum;

            double mutationRate = parameters.mutationRate();
            if (mutationRate > 0.0) {
                double uniform = mutationRate / 3.0;
                nextCooperators = (1.0 - mutationRate) * nextCooperators + uniform;
                nextDefectors = (1.0 - mutationRate) * nextDefectors + uniform;
                nextLoners = (1.0 - mutationRate) * nextLoners + uniform;
            }

            double totalAfterMutation = nextCooperators + nextDefectors + nextLoners;
            cooperatorShare = nextCooperators / totalAfterMutation;
            defectorShare = nextDefectors / totalAfterMutation;
            lonerShare = nextLoners / totalAfterMutation;
        }

        return new PublicGoodsResult(List.copyOf(history));
    }

    private Totals simulateGeneration(Random random,
                                      double cooperatorShare,
                                      double defectorShare,
                                      double lonerShare) {
        double totalCooperatorPayoff = 0.0;
        double totalDefectorPayoff = 0.0;
        double totalLonerPayoff = 0.0;

        int cooperatorAppearances = 0;
        int defectorAppearances = 0;
        int lonerAppearances = 0;

        for (int interaction = 0; interaction < parameters.interactionsPerGeneration(); interaction++) {
            int cooperators = 0;
            int defectors = 0;
            int loners = 0;

            for (int slot = 0; slot < parameters.groupSize(); slot++) {
                double sample = random.nextDouble();
                if (sample < cooperatorShare) {
                    cooperators++;
                } else if (sample < cooperatorShare + defectorShare) {
                    defectors++;
                } else {
                    loners++;
                }
            }

            if (loners == parameters.groupSize()) {
                totalLonerPayoff += loners * parameters.lonerPayoff();
                lonerAppearances += loners;
                continue;
            }

            int participants = cooperators + defectors;
            if (participants <= 1) {
                if (cooperators == 1) {
                    totalCooperatorPayoff += parameters.lonerPayoff();
                    cooperatorAppearances++;
                }
                if (defectors == 1) {
                    totalDefectorPayoff += parameters.lonerPayoff();
                    defectorAppearances++;
                }
                if (loners > 0) {
                    totalLonerPayoff += loners * parameters.lonerPayoff();
                    lonerAppearances += loners;
                }
                continue;
            }

            double totalContribution = cooperators * parameters.contributionCost();
            double pot = totalContribution * parameters.multiplier();
            double perParticipant = pot / participants;

            totalCooperatorPayoff += cooperators * (perParticipant - parameters.contributionCost());
            totalDefectorPayoff += defectors * perParticipant;
            totalLonerPayoff += loners * parameters.lonerPayoff();

            cooperatorAppearances += cooperators;
            defectorAppearances += defectors;
            lonerAppearances += loners;
        }

        double averageCooperatorPayoff = cooperatorAppearances > 0
                ? totalCooperatorPayoff / cooperatorAppearances
                : parameters.lonerPayoff();
        double averageDefectorPayoff = defectorAppearances > 0
                ? totalDefectorPayoff / defectorAppearances
                : parameters.lonerPayoff();
        double averageLonerPayoff = lonerAppearances > 0
                ? totalLonerPayoff / lonerAppearances
                : parameters.lonerPayoff();

        double populationPayoff = cooperatorShare * averageCooperatorPayoff
                + defectorShare * averageDefectorPayoff
                + lonerShare * averageLonerPayoff;

        return new Totals(
                averageCooperatorPayoff,
                averageDefectorPayoff,
                averageLonerPayoff,
                populationPayoff
        );
    }

    private record Totals(
            double averageCooperatorPayoff,
            double averageDefectorPayoff,
            double averageLonerPayoff,
            double populationPayoff
    ) {
    }
}
