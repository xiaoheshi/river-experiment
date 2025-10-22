package com.river.experiment.staghunt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 鹿猎博弈带信号机制的蒙特卡洛复制器模拟。
 */
public final class StagHuntSimulation {

    private static final double EPSILON = 1.0e-8;

    private final StagHuntParameters parameters;

    public StagHuntSimulation(StagHuntParameters parameters) {
        this.parameters = parameters;
    }

    public StagHuntResult run() {
        Random random = new Random(parameters.seed());
        List<StagHuntGeneration> history = new ArrayList<>();

        double signalerShare = parameters.initialSignalerShare();
        double followerShare = parameters.initialFollowerShare();
        double lonerShare = parameters.initialLonerShare();

        for (int generation = 0; generation <= parameters.generations(); generation++) {
            Totals totals = simulateGeneration(random, signalerShare, followerShare, lonerShare);

            history.add(new StagHuntGeneration(
                    generation,
                    signalerShare,
                    followerShare,
                    lonerShare,
                    totals.averageSignalerPayoff,
                    totals.averageFollowerPayoff,
                    totals.averageLonerPayoff,
                    totals.populationPayoff,
                    totals.stagSuccessRate,
                    totals.signalActivationRate,
                    totals.stagConversionRate
            ));

            if (generation == parameters.generations()) {
                break;
            }

            double selectionStrength = parameters.selectionStrength();
            double nextSignaler = signalerShare + selectionStrength * signalerShare * (totals.averageSignalerPayoff - totals.populationPayoff);
            double nextFollower = followerShare + selectionStrength * followerShare * (totals.averageFollowerPayoff - totals.populationPayoff);
            double nextLoner = lonerShare + selectionStrength * lonerShare * (totals.averageLonerPayoff - totals.populationPayoff);

            nextSignaler = Math.max(EPSILON, nextSignaler);
            nextFollower = Math.max(EPSILON, nextFollower);
            nextLoner = Math.max(EPSILON, nextLoner);

            double sum = nextSignaler + nextFollower + nextLoner;
            nextSignaler /= sum;
            nextFollower /= sum;
            nextLoner /= sum;

            double mutationRate = parameters.mutationRate();
            if (mutationRate > 0.0) {
                double uniform = mutationRate / 3.0;
                nextSignaler = (1.0 - mutationRate) * nextSignaler + uniform;
                nextFollower = (1.0 - mutationRate) * nextFollower + uniform;
                nextLoner = (1.0 - mutationRate) * nextLoner + uniform;
            }

            double totalAfterMutation = nextSignaler + nextFollower + nextLoner;
            signalerShare = nextSignaler / totalAfterMutation;
            followerShare = nextFollower / totalAfterMutation;
            lonerShare = nextLoner / totalAfterMutation;
        }

        return new StagHuntResult(List.copyOf(history));
    }

    private Totals simulateGeneration(Random random,
                                      double signalerShare,
                                      double followerShare,
                                      double lonerShare) {
        double totalSignalerPayoff = 0.0;
        double totalFollowerPayoff = 0.0;
        double totalLonerPayoff = 0.0;

        int signalerAppearances = 0;
        int followerAppearances = 0;
        int lonerAppearances = 0;

        int successfulStagHunts = 0;
        int stagAttempts = 0;
        int signalBroadcasts = 0;

        for (int interaction = 0; interaction < parameters.interactionsPerGeneration(); interaction++) {
            Strategy first = sampleStrategy(random, signalerShare, followerShare);
            Strategy second = sampleStrategy(random, signalerShare, followerShare);

            InteractionOutcome outcome = playInteraction(first, second);
            totalSignalerPayoff += outcome.firstSignalerPayoff;
            totalFollowerPayoff += outcome.firstFollowerPayoff;
            totalLonerPayoff += outcome.firstLonerPayoff;
            signalerAppearances += outcome.firstSignalerCount;
            followerAppearances += outcome.firstFollowerCount;
            lonerAppearances += outcome.firstLonerCount;

            totalSignalerPayoff += outcome.secondSignalerPayoff;
            totalFollowerPayoff += outcome.secondFollowerPayoff;
            totalLonerPayoff += outcome.secondLonerPayoff;
            signalerAppearances += outcome.secondSignalerCount;
            followerAppearances += outcome.secondFollowerCount;
            lonerAppearances += outcome.secondLonerCount;

            if (outcome.signalBroadcast) {
                signalBroadcasts++;
            }
            if (outcome.stagAttempt) {
                stagAttempts++;
            }
            if (outcome.stagSuccess) {
                successfulStagHunts++;
            }
        }

        double averageSignalerPayoff = signalerAppearances > 0
                ? totalSignalerPayoff / signalerAppearances
                : parameters.harePayoff();
        double averageFollowerPayoff = followerAppearances > 0
                ? totalFollowerPayoff / followerAppearances
                : parameters.harePayoff();
        double averageLonerPayoff = lonerAppearances > 0
                ? totalLonerPayoff / lonerAppearances
                : parameters.harePayoff();

        double populationPayoff = signalerShare * averageSignalerPayoff
                + followerShare * averageFollowerPayoff
                + lonerShare * averageLonerPayoff;

        double interactions = parameters.interactionsPerGeneration();
        double stagSuccessRate = successfulStagHunts / interactions;
        double signalActivationRate = signalBroadcasts / interactions;
        double stagConversionRate = stagAttempts > 0
                ? (double) successfulStagHunts / stagAttempts
                : 0.0;

        return new Totals(
                averageSignalerPayoff,
                averageFollowerPayoff,
                averageLonerPayoff,
                populationPayoff,
                stagSuccessRate,
                signalActivationRate,
                stagConversionRate
        );
    }

    private Strategy sampleStrategy(Random random,
                                    double signalerShare,
                                    double followerShare) {
        double draw = random.nextDouble();
        if (draw < signalerShare) {
            return Strategy.SIGNALER;
        } else if (draw < signalerShare + followerShare) {
            return Strategy.FOLLOWER;
        }
        return Strategy.LONER;
    }

    private InteractionOutcome playInteraction(Strategy first, Strategy second) {
        boolean firstSignals = first == Strategy.SIGNALER;
        boolean secondSignals = second == Strategy.SIGNALER;
        boolean signalBroadcast = firstSignals || secondSignals;

        boolean firstHuntsStag = firstSignals || (first == Strategy.FOLLOWER && secondSignals);
        boolean secondHuntsStag = secondSignals || (second == Strategy.FOLLOWER && firstSignals);

        boolean stagAttempt = firstHuntsStag || secondHuntsStag;
        boolean stagSuccess = firstHuntsStag && secondHuntsStag;

        double firstPayoff;
        double secondPayoff;

        if (stagSuccess) {
            firstPayoff = parameters.stagPayoff();
            secondPayoff = parameters.stagPayoff();
        } else if (firstHuntsStag && !secondHuntsStag) {
            firstPayoff = parameters.failedStagPayoff();
            secondPayoff = parameters.harePayoff();
        } else if (!firstHuntsStag && secondHuntsStag) {
            firstPayoff = parameters.harePayoff();
            secondPayoff = parameters.failedStagPayoff();
        } else {
            firstPayoff = parameters.harePayoff();
            secondPayoff = parameters.harePayoff();
        }

        if (firstSignals) {
            firstPayoff -= parameters.signalCost();
        }
        if (secondSignals) {
            secondPayoff -= parameters.signalCost();
        }

        double firstSignalerPayoff = firstSignals ? firstPayoff : 0.0;
        double firstFollowerPayoff = first == Strategy.FOLLOWER ? firstPayoff : 0.0;
        double firstLonerPayoff = first == Strategy.LONER ? firstPayoff : 0.0;
        double secondSignalerPayoff = secondSignals ? secondPayoff : 0.0;
        double secondFollowerPayoff = second == Strategy.FOLLOWER ? secondPayoff : 0.0;
        double secondLonerPayoff = second == Strategy.LONER ? secondPayoff : 0.0;

        return new InteractionOutcome(
                firstSignals ? 1 : 0,
                first == Strategy.FOLLOWER ? 1 : 0,
                first == Strategy.LONER ? 1 : 0,
                secondSignals ? 1 : 0,
                second == Strategy.FOLLOWER ? 1 : 0,
                second == Strategy.LONER ? 1 : 0,
                firstSignalerPayoff,
                firstFollowerPayoff,
                firstLonerPayoff,
                secondSignalerPayoff,
                secondFollowerPayoff,
                secondLonerPayoff,
                signalBroadcast,
                stagAttempt,
                stagSuccess
        );
    }

    private enum Strategy {
        SIGNALER,
        FOLLOWER,
        LONER
    }

    private record InteractionOutcome(
            int firstSignalerCount,
            int firstFollowerCount,
            int firstLonerCount,
            int secondSignalerCount,
            int secondFollowerCount,
            int secondLonerCount,
            double firstSignalerPayoff,
            double firstFollowerPayoff,
            double firstLonerPayoff,
            double secondSignalerPayoff,
            double secondFollowerPayoff,
            double secondLonerPayoff,
            boolean signalBroadcast,
            boolean stagAttempt,
            boolean stagSuccess
    ) {
    }

    private record Totals(
            double averageSignalerPayoff,
            double averageFollowerPayoff,
            double averageLonerPayoff,
            double populationPayoff,
            double stagSuccessRate,
            double signalActivationRate,
            double stagConversionRate
    ) {
    }
}
