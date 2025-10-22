package com.river.experiment;

import com.river.experiment.cooperation.CooperationExperiment;
import com.river.experiment.hawkdove.HawkDoveExperiment;
import com.river.experiment.kinselection.KinSelectionExperiment;
import com.river.experiment.publicgoods.PublicGoodsExperiment;
import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;
import com.river.experiment.core.article.ArticleExportResult;
import com.river.experiment.core.article.MarkdownArticleWriter;
import com.river.experiment.core.chart.ChartAttachment;
import com.river.experiment.staghunt.StagHuntExperiment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 一键运行全部演化实验，生成单篇合集文章与各自的图文报告。
 */
public final class AllExperimentsApp {

    private static final Path ARTICLES_DIRECTORY = Path.of("articles", "generated");
    private static final Path ASSETS_ROOT = ARTICLES_DIRECTORY.resolve("assets");
    private static final String DIGEST_SLUG = "evolution-digest";

    public static void main(String[] args) {
        List<Entry<?>> entries = new ArrayList<>();

        runAndCollect(new CooperationExperiment(), entries);

        KinSelectionExperiment kinSelectionExperiment = new KinSelectionExperiment(
                new com.river.experiment.kinselection.SimulationParameters(
                        6000,
                        6,
                        1000,
                        1.0,
                        2.4,
                        0.8,
                        0.02,
                        0.5,
                        0.25
                ),
                42L
        );
        runAndCollect(kinSelectionExperiment, entries);

        HawkDoveExperiment hawkDoveExperiment = new HawkDoveExperiment(
                new com.river.experiment.hawkdove.SimulationParameters(
                        240,
                        0.65,
                        2.0,
                        6.0,
                        0.01,
                        0.45
                )
        );
        runAndCollect(hawkDoveExperiment, entries);

        PublicGoodsExperiment publicGoodsExperiment = new PublicGoodsExperiment(
                new com.river.experiment.publicgoods.PublicGoodsParameters(
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
                )
        );
        runAndCollect(publicGoodsExperiment, entries);

        StagHuntExperiment stagHuntExperiment = new StagHuntExperiment(
                new com.river.experiment.staghunt.StagHuntParameters(
                        240,
                        7500,
                        0.72,
                        0.018,
                        5.0,
                        2.0,
                        0.0,
                        0.8,
                        0.12,
                        0.58,
                        0.30,
                        2028L
                )
        );
        runAndCollect(stagHuntExperiment, entries);

        try {
            Path digestPath = writeDigest(entries);
            System.out.println();
            System.out.println("合集 Markdown 文章：");
            System.out.println("  " + digestPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("生成合集文章失败：" + e.getMessage());
        }
    }

    private static <R extends ExperimentReport> void runAndCollect(Experiment<R> experiment,
                                                                   List<Entry<?>> entries) {
        R report = experiment.run();
        System.out.println("【运行完成】" + experiment.displayName() + " → " + report.sectionTitle());

        try {
            ArticleExportResult exportResult = MarkdownArticleWriter.write(
                    ARTICLES_DIRECTORY,
                    experiment.id(),
                    report,
                    ASSETS_ROOT
            );
            entries.add(new Entry<>(experiment.id(), experiment.displayName(), report, exportResult));
        } catch (IOException e) {
            System.err.println("导出 " + experiment.displayName() + " Markdown 失败：" + e.getMessage());
        }
    }

    private static Path writeDigest(List<Entry<?>> entries) throws IOException {
        Files.createDirectories(ARTICLES_DIRECTORY);
        Path digestPath = ARTICLES_DIRECTORY.resolve(DIGEST_SLUG + ".md");

        try (BufferedWriter writer = Files.newBufferedWriter(digestPath, StandardCharsets.UTF_8)) {
            writer.write("# 演化合作爆款素材库");
            writer.newLine();
            writer.newLine();
            writer.write("这是一键跑完五个演化实验后的“公众号素材套餐”：直接带上数据、图表与写作提示，帮你从家庭互助讲到价格战、公共物品与协调破局。");
            writer.newLine();
            writer.newLine();
            writer.write("使用方法很简单：每段保留核心叙事，挑选适合的图表，就能组合成一篇爆款潜质文章或多篇专题稿。");
            writer.newLine();
            writer.newLine();

            for (Entry<?> entry : entries) {
                ExperimentReport report = entry.report();
                writer.write("## " + report.sectionTitle());
                writer.newLine();
                writer.newLine();
                writer.write("> 来源：" + entry.displayName());
                writer.newLine();
                writer.newLine();

                for (String paragraph : report.paragraphs()) {
                    writer.write(paragraph);
                    writer.newLine();
                    writer.newLine();
                }

                List<ChartAttachment> attachments = report.charts();
                List<Path> chartFiles = entry.exportResult().chartFiles();
                for (int i = 0; i < attachments.size(); i++) {
                    ChartAttachment attachment = attachments.get(i);
                    Path chartFile = chartFiles.get(i);
                    Path relative = ARTICLES_DIRECTORY.relativize(chartFile);
                    writer.write("![");
                    writer.write(attachment.title());
                    writer.write("](");
                    writer.write(relative.toString().replace('\\', '/'));
                    writer.write(")");
                    writer.newLine();
                    writer.newLine();
                }
            }
        }

        return digestPath;
    }

    private record Entry<R extends ExperimentReport>(
            String slug,
            String displayName,
            R report,
            ArticleExportResult exportResult
    ) {
    }

    private AllExperimentsApp() {
    }
}
