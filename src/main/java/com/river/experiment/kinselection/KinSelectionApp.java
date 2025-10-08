package com.river.experiment.kinselection;

/**
 * 程序入口，负责运行亲缘选择模拟并打印汇总指标。
 */
public final class KinSelectionApp {

    public static void main(String[] args) {
        SimulationParameters parameters = new SimulationParameters(
                6000,     // 总人口数量
                6,       // 每个家庭的成员数
                1000,      // 模拟的代数
                1.0,     // 基础适合度
                2.4,     // 利他的亲属收益
                0.8,     // 利他者自付成本
                0.02,    // 策略突变率
                0.5,     // 家庭内近亲相关系数（约等同兄弟姐妹）
                0.25     // 初始利他者占比
        );

        KinSelectionSimulation simulation = new KinSelectionSimulation(parameters, 42L);
        SimulationResult result = simulation.run();

        System.out.println("亲缘选择模拟（验证汉密尔顿法则）");
        System.out.println("参数设定：");
        System.out.printf("  人口=%d, 家庭规模=%d, 代数=%d%n",
                parameters.populationSize(), parameters.familySize(), parameters.generations());
        System.out.printf("  收益=%.2f, 成本=%.2f, 相关系数=%.2f, 突变率=%.2f%n",
                parameters.benefit(), parameters.cost(), parameters.relatednessWithinFamily(), parameters.mutationRate());

        for (GenerationStats stats : result.generations()) {
            if (shouldPrintGeneration(stats, result)) {
                printGeneration(stats);
            }
        }

        GenerationStats finalStats = result.lastGeneration();
        StrategySnapshot altruists = finalStats.snapshot(Strategy.ALTRUIST);
        StrategySnapshot selfish = finalStats.snapshot(Strategy.SELFISH);

        System.out.println();
        System.out.println("最终结论：");
        System.out.printf("  利他者占比：%.1f%%%n", 100 * finalStats.share(Strategy.ALTRUIST));
        System.out.printf("  自私者占比：%.1f%%%n", 100 * finalStats.share(Strategy.SELFISH));
        System.out.printf("  利他者包容适合度优势：%.2f%n",
                altruists.averageInclusiveFitness() - selfish.averageInclusiveFitness());
        System.out.printf("  利他者直接适合度劣势：%.2f%n",
                altruists.averageDirectFitness() - selfish.averageDirectFitness());
    }

    private static boolean shouldPrintGeneration(GenerationStats stats, SimulationResult result) {
        int lastGenerationIndex = result.generations().size() - 1;
        return stats.generation() == 0
                || stats.generation() == lastGenerationIndex
                || stats.generation() % 10 == 0;
    }

    private static void printGeneration(GenerationStats stats) {
        StrategySnapshot altruists = stats.snapshot(Strategy.ALTRUIST);
        StrategySnapshot selfish = stats.snapshot(Strategy.SELFISH);
        System.out.printf(
                "第 %2d 代 | 利他者: %3d (%.1f%%) 直接=%.3f 包容=%.3f | 自私者: %3d (%.1f%%) 直接=%.3f 包容=%.3f%n",
                stats.generation(),
                altruists.count(),
                100 * stats.share(Strategy.ALTRUIST),
                altruists.averageDirectFitness(),
                altruists.averageInclusiveFitness(),
                selfish.count(),
                100 * stats.share(Strategy.SELFISH),
                selfish.averageDirectFitness(),
                selfish.averageInclusiveFitness()
        );
    }

    private KinSelectionApp() {
    }
}
