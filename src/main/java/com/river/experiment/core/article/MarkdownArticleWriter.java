package com.river.experiment.core.article;

import com.river.experiment.core.ExperimentReport;
import com.river.experiment.core.chart.ChartAttachment;
import com.river.experiment.core.chart.ChartRenderer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * 将 {@link ExperimentReport} 渲染为微信公众号可直接引用的 Markdown 文章。
 */
public final class MarkdownArticleWriter {

    private MarkdownArticleWriter() {
    }

    /**
     * 根据实验报告导出 Markdown 文章与图表。
     *
     * @param articleDirectory Markdown 文件输出目录
     * @param slug             文件名（不含扩展名），通常使用实验 id
     * @param report           实验报告
     * @param assetsRoot       图表资产根目录，会在其中创建 {@code slug} 子目录
     * @return 文章与图表的输出结果
     */
    public static ArticleExportResult write(Path articleDirectory,
                                            String slug,
                                            ExperimentReport report,
                                            Path assetsRoot) throws IOException {
        Objects.requireNonNull(articleDirectory, "articleDirectory");
        Objects.requireNonNull(slug, "slug");
        Objects.requireNonNull(report, "report");
        Objects.requireNonNull(assetsRoot, "assetsRoot");

        Files.createDirectories(articleDirectory);
        Path assetsDirectory = assetsRoot.resolve(slug);
        List<ChartAttachment> attachments = report.charts();
        List<Path> chartFiles = ChartRenderer.renderAll(assetsDirectory, attachments);

        Path articlePath = articleDirectory.resolve(slug + ".md");
        try (BufferedWriter writer = Files.newBufferedWriter(articlePath, StandardCharsets.UTF_8)) {
            writer.write("# ");
            writer.write(report.sectionTitle());
            writer.newLine();
            writer.newLine();

            for (String paragraph : report.paragraphs()) {
                writer.write(paragraph);
                writer.newLine();
                writer.newLine();
            }

            for (int i = 0; i < attachments.size(); i++) {
                ChartAttachment attachment = attachments.get(i);
                Path chartPath = chartFiles.get(i);
                Path relative = articleDirectory.relativize(chartPath);
                writer.write("![");
                writer.write(attachment.title());
                writer.write("](");
                writer.write(relative.toString().replace('\\', '/'));
                writer.write(")");
                writer.newLine();
                writer.newLine();
            }
        }

        return new ArticleExportResult(articlePath, chartFiles);
    }
}
