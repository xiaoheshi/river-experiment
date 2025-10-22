package com.river.experiment.staghunt;

import java.nio.file.Path;
import java.util.List;

import com.river.experiment.core.article.ArticleExportResult;
import com.river.experiment.core.article.MarkdownArticleWriter;

/**
 * 鹿猎信号协调实验入口。
 */
public final class StagHuntApp {

    public static void main(String[] args) {
        StagHuntParameters parameters = new StagHuntParameters(
                260,
                8000,
                0.72,
                0.018,
                5.0,
                2.0,
                0.0,
                0.8,
                0.12,
                0.58,
                0.30,
                2031L
        );

        StagHuntExperiment experiment = new StagHuntExperiment(parameters);
        StagHuntExperiment.StagHuntReport report = experiment.run();

        printSummary(report);
        exportArticle(experiment, report);
    }

    private static void printSummary(StagHuntExperiment.StagHuntReport report) {
        StagHuntParameters params = report.parameters();
        StagHuntGeneration first = report.firstGeneration();
        StagHuntGeneration last = report.lastGeneration();

        System.out.println("鹿猎博弈：信号者 vs 跟随者 vs 保守者");
        System.out.printf("参数：代数=%d, 每代对局=%d, 鹿猎收益=%.1f, 猎兔收益=%.1f, 信号成本=%.1f, 选择强度=%.2f, 突变率=%.3f%n",
                params.generations(),
                params.interactionsPerGeneration(),
                params.stagPayoff(),
                params.harePayoff(),
                params.signalCost(),
                params.selectionStrength(),
                params.mutationRate());
        System.out.printf("初始占比：信号者 %.1f%% / 跟随者 %.1f%% / 保守者 %.1f%%%n",
                percentage(first.signalerShare()),
                percentage(first.followerShare()),
                percentage(first.lonerShare()));

        List<StagHuntGeneration> generations = report.result().generations();
        int lastIndex = generations.get(generations.size() - 1).generation();
        for (StagHuntGeneration generation : generations) {
            if (shouldPrint(generation.generation(), lastIndex)) {
                System.out.printf(
                        "第 %3d 代 | 信号 %.1f%% (%.3f) | 跟随 %.1f%% (%.3f) | 保守 %.1f%% (%.3f) | 信号触发 %.1f%% | 鹿猎成功 %.1f%%%n",
                        generation.generation(),
                        percentage(generation.signalerShare()),
                        generation.signalerPayoff(),
                        percentage(generation.followerShare()),
                        generation.followerPayoff(),
                        percentage(generation.lonerShare()),
                        generation.lonerPayoff(),
                        percentage(generation.signalActivationRate()),
                        percentage(generation.stagSuccessRate())
                );
            }
        }

        StagHuntGeneration peak = report.peakGeneration();
        System.out.println();
        System.out.printf("最终占比：信号者 %.1f%%，跟随者 %.1f%%，保守者 %.1f%%，群体收益 %.3f%n",
                percentage(last.signalerShare()),
                percentage(last.followerShare()),
                percentage(last.lonerShare()),
                last.populationPayoff());
        System.out.printf("最高鹿猎成功率：第 %d 代 %.1f%%；信号突破点：%s；信号转化拐点：%s%n",
                peak.generation(),
                percentage(peak.stagSuccessRate()),
                report.breakthroughGeneration() >= 0 ? ("第 " + report.breakthroughGeneration() + " 代") : "模拟范围内未出现",
                report.coordinationFlipGeneration() >= 0 ? ("第 " + report.coordinationFlipGeneration() + " 代") : "模拟范围内未出现");
    }

    private static void exportArticle(StagHuntExperiment experiment, StagHuntExperiment.StagHuntReport report) {
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

    private static boolean shouldPrint(int generation, int lastGeneration) {
        return generation == 0 || generation == lastGeneration || generation % 30 == 0;
    }

    private static double percentage(double value) {
        return 100.0 * value;
    }

    private StagHuntApp() {
    }
}
