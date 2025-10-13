package com.river.experiment.publicgoods;

/**
 * 公共物品博弈模拟入口。
 */
public final class PublicGoodsApp {

    public static void main(String[] args) {
        PublicGoodsParameters parameters = new PublicGoodsParameters(
                250,
                5,
                8000,
                3.0,
                1.0,
                1.2,
                0.015,
                0.45,
                0.5,
                0.35,
                2026L
        );

        PublicGoodsSimulation simulation = new PublicGoodsSimulation(parameters);
        PublicGoodsResult result = simulation.run();

        PublicGoodsGeneration first = result.firstGeneration();
        System.out.println("公共物品博弈：合作者 vs 搭便车者 vs 旁观者");
        System.out.printf("参数：代数=%d, 组规模=%d, 互动/代=%d, 乘数=%.2f, 成本=%.2f, 旁观者收益=%.2f%n",
                parameters.generations(),
                parameters.groupSize(),
                parameters.interactionsPerGeneration(),
                parameters.multiplier(),
                parameters.contributionCost(),
                parameters.lonerPayoff());
        System.out.printf("初始占比：合作者 %.1f%% / 搭便车者 %.1f%% / 旁观者 %.1f%%%n",
                percentage(first.cooperatorShare()),
                percentage(first.defectorShare()),
                percentage(first.lonerShare()));

        for (PublicGoodsGeneration generation : result.generations()) {
            if (shouldPrint(generation, parameters.generations())) {
                System.out.printf(
                        "第 %3d 代 | 合作 %.1f%% (%.3f) | 搭便 %.1f%% (%.3f) | 旁观 %.1f%% (%.3f) | 平均收益 %.3f%n",
                        generation.generation(),
                        percentage(generation.cooperatorShare()),
                        generation.cooperatorPayoff(),
                        percentage(generation.defectorShare()),
                        generation.defectorPayoff(),
                        percentage(generation.lonerShare()),
                        generation.lonerPayoff(),
                        generation.populationPayoff()
                );
            }
        }

        PublicGoodsGeneration last = result.lastGeneration();
        System.out.println();
        System.out.printf("最终占比：合作者 %.1f%%，搭便车者 %.1f%%，旁观者 %.1f%%，群体平均收益 %.3f%n",
                percentage(last.cooperatorShare()),
                percentage(last.defectorShare()),
                percentage(last.lonerShare()),
                last.populationPayoff());
    }

    private static boolean shouldPrint(PublicGoodsGeneration generation, int generations) {
        return generation.generation() == 0
                || generation.generation() == generations
                || generation.generation() % 25 == 0;
    }

    private static double percentage(double value) {
        return 100.0 * value;
    }

    private PublicGoodsApp() {
    }
}
