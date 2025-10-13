package com.river.experiment.publicgoods;

import java.nio.file.Path;
import java.util.List;

import com.river.experiment.core.article.ArticleExportResult;
import com.river.experiment.core.article.MarkdownArticleWriter;

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

        PublicGoodsExperiment experiment = new PublicGoodsExperiment(parameters);
        PublicGoodsExperiment.PublicGoodsReport report = experiment.run();

        printSummary(report);
        exportArticle(experiment, report);
    }

    private static void printSummary(PublicGoodsExperiment.PublicGoodsReport report) {
        PublicGoodsParameters params = report.parameters();
        PublicGoodsResult result = report.result();
        PublicGoodsGeneration first = result.firstGeneration();
        PublicGoodsGeneration last = result.lastGeneration();

        System.out.println("公共物品博弈：合作者 vs 搭便车者 vs 旁观者");
        System.out.printf("参数：代数=%d, 组规模=%d, 每代互动=%d, 乘数=%.2f, 成本=%.2f, 旁观者收益=%.2f, 复制强度=%.2f, 突变率=%.3f%n",
                params.generations(),
                params.groupSize(),
                params.interactionsPerGeneration(),
                params.multiplier(),
                params.contributionCost(),
                params.lonerPayoff(),
                params.selectionStrength(),
                params.mutationRate());
        System.out.printf("初始占比：合作者 %.1f%% / 搭便车者 %.1f%% / 旁观者 %.1f%%%n",
                percentage(first.cooperatorShare()),
                percentage(first.defectorShare()),
                percentage(first.lonerShare()));

        List<PublicGoodsGeneration> generations = result.generations();
        int lastGeneration = generations.get(generations.size() - 1).generation();
        for (PublicGoodsGeneration generation : generations) {
            if (shouldPrint(generation, lastGeneration)) {
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

        PublicGoodsGeneration peak = report.peakGeneration();
        System.out.println();
        System.out.printf("最终占比：合作者 %.1f%%，搭便车者 %.1f%%，旁观者 %.1f%%，群体平均收益 %.3f%n",
                percentage(last.cooperatorShare()),
                percentage(last.defectorShare()),
                percentage(last.lonerShare()),
                last.populationPayoff());
        System.out.printf("最高合作率：第 %d 代 %.1f%%；搭便车降至 20%% 以下的代数：%s%n",
                peak.generation(),
                percentage(peak.cooperatorShare()),
                report.suppressionGeneration() >= 0 ? ("第 " + report.suppressionGeneration() + " 代") : "未达成");
    }

    private static void exportArticle(PublicGoodsExperiment experiment, PublicGoodsExperiment.PublicGoodsReport report) {
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

    private static boolean shouldPrint(PublicGoodsGeneration generation, int lastGeneration) {
        return generation.generation() == 0
                || generation.generation() == lastGeneration
                || generation.generation() % 25 == 0;
    }

    private static double percentage(double value) {
        return 100.0 * value;
    }

    private PublicGoodsApp() {
    }
}
