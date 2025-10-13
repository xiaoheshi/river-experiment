package com.river.experiment.hawkdove;

import java.nio.file.Path;
import java.util.List;

import com.river.experiment.core.article.ArticleExportResult;
import com.river.experiment.core.article.MarkdownArticleWriter;

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

        HawkDoveExperiment experiment = new HawkDoveExperiment(parameters);
        HawkDoveExperiment.HawkDoveReport report = experiment.run();

        printSummary(report);
        exportArticle(experiment, report);
    }

    private static void printSummary(HawkDoveExperiment.HawkDoveReport report) {
        SimulationParameters parameters = report.parameters();
        GenerationState first = report.result().firstGeneration();
        GenerationState last = report.result().lastGeneration();

        System.out.println("鹰鸽冲突（经典资源争夺博弈）");
        System.out.printf("参数：代数=%d, 初始鹰派=%.1f%%, V=%.1f, C=%.1f, 复制强度=%.2f, 突变率=%.2f%n",
                parameters.generations(),
                percentage(first.hawkShare()),
                parameters.resourceValue(),
                parameters.conflictCost(),
                parameters.selectionStrength(),
                parameters.mutationRate());
        System.out.printf("理论均衡鹰派占比：%.1f%%%n", percentage(report.essShare()));

        List<GenerationState> generations = report.result().generations();
        int lastGenerationIndex = generations.get(generations.size() - 1).generation();
        for (GenerationState state : generations) {
            if (shouldPrint(state, lastGenerationIndex)) {
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
        System.out.printf("最终：鹰派 %.1f%%，鸽派 %.1f%%，收益差 %.3f；首次收敛代数：%s%n",
                percentage(last.hawkShare()),
                percentage(last.doveShare()),
                last.hawkPayoff() - last.dovePayoff(),
                report.stabilizationGeneration() >= 0 ? ("第 " + report.stabilizationGeneration() + " 代") : "尚未进入 ±2% 区间");
    }

    private static void exportArticle(HawkDoveExperiment experiment, HawkDoveExperiment.HawkDoveReport report) {
        Path articlesDirectory = Path.of("articles", "generated");
        Path assetsRoot = articlesDirectory.resolve("assets");
        try {
            ArticleExportResult exportResult = MarkdownArticleWriter.write(
                    articlesDirectory,
                    experiment.id(),
                    report,
                    assetsRoot
            );
            System.out.println();
            if (!exportResult.chartFiles().isEmpty()) {
                System.out.println("图表输出：");
                exportResult.chartFiles()
                        .forEach(path -> System.out.println("  " + path.toAbsolutePath()));
            }
            System.out.println("Markdown 文章：");
            System.out.println("  " + exportResult.articlePath().toAbsolutePath());
        } catch (Exception e) {
            System.err.println("导出 Markdown 失败：" + e.getMessage());
        }
    }

    private static boolean shouldPrint(GenerationState state, int lastGeneration) {
        return state.generation() == 0
                || state.generation() == lastGeneration
                || state.generation() % 20 == 0;
    }

    private static double percentage(double value) {
        return 100.0 * value;
    }

    private HawkDoveApp() {
    }
}
