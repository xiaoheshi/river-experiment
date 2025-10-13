package com.river.experiment.hawkdove;

/**
 * 鹰鸽冲突模拟入口。
 */
public final class HawkDoveApp {

    public static void main(String[] args) {
        SimulationParameters parameters = new SimulationParameters(
                240,
                0.65,
                2.0,
                6.0,
                0.01,
                0.45
        );

        HawkDoveSimulation simulation = new HawkDoveSimulation(parameters);
        SimulationResult result = simulation.run();

        GenerationState first = result.firstGeneration();
        GenerationState last = result.lastGeneration();

        System.out.println("鹰鸽冲突（经典资源争夺博弈）");
        System.out.printf("参数：代数=%d, 初始鹰派=%.1f%%, V=%.1f, C=%.1f, 复制强度=%.2f, 突变率=%.2f%n",
                parameters.generations(),
                percentage(first.hawkShare()),
                parameters.resourceValue(),
                parameters.conflictCost(),
                parameters.selectionStrength(),
                parameters.mutationRate());

        for (GenerationState state : result.generations()) {
            if (shouldPrint(state, parameters.generations())) {
                System.out.printf(
                        "第 %3d 代 | 鹰派 %.1f%% (收益 %.3f) | 鸽派 %.1f%% (收益 %.3f) | 平均收益 %.3f%n",
                        state.generation(),
                        percentage(state.hawkShare()),
                        state.hawkPayoff(),
                        percentage(state.doveShare()),
                        state.dovePayoff(),
                        state.averagePayoff()
                );
            }
        }

        System.out.println();
        System.out.printf("最终：鹰派 %.1f%%，鸽派 %.1f%%，收益差 %.3f%n",
                percentage(last.hawkShare()),
                percentage(last.doveShare()),
                last.hawkPayoff() - last.dovePayoff());
    }

    private static boolean shouldPrint(GenerationState state, int generations) {
        return state.generation() == 0
                || state.generation() == generations
                || state.generation() % 20 == 0;
    }

    private static double percentage(double value) {
        return 100.0 * value;
    }

    private HawkDoveApp() {
    }
}
