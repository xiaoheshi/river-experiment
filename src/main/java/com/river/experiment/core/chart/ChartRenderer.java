package com.river.experiment.core.chart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.SeriesMarkers;

/**
 * 基于 XChart 的图表渲染器，负责将 {@link ChartAttachment} 输出为 PNG 文件。
 */
public final class ChartRenderer {

    private static final int DEFAULT_WIDTH = 960;
    private static final int DEFAULT_HEIGHT = 540;

    private ChartRenderer() {
    }

    public static List<Path> renderAll(Path outputDirectory, List<ChartAttachment> attachments) throws IOException {
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(attachments, "attachments");
        List<Path> createdFiles = new ArrayList<>();
        if (attachments.isEmpty()) {
            return createdFiles;
        }

        Files.createDirectories(outputDirectory);
        for (ChartAttachment attachment : attachments) {
            XYChart chart = new XYChartBuilder()
                    .width(DEFAULT_WIDTH)
                    .height(DEFAULT_HEIGHT)
                    .title(attachment.title())
                    .xAxisTitle(attachment.xAxisLabel())
                    .yAxisTitle(attachment.yAxisLabel())
                    .build();
            chart.getStyler().setLegendVisible(true);
            chart.getStyler().setMarkerSize(6);

            attachment.series().forEach(series -> {
                chart.addSeries(series.name(), series.xValues(), series.yValues())
                        .setMarker(SeriesMarkers.NONE);
            });

            String fileName = attachment.fileName().endsWith(".png")
                    ? attachment.fileName()
                    : attachment.fileName() + ".png";
            Path filePath = outputDirectory.resolve(fileName);
            String bitmapBasePath = stripPngExtension(filePath);
            BitmapEncoder.saveBitmap(chart, bitmapBasePath, BitmapFormat.PNG);
            createdFiles.add(filePath);
        }
        return createdFiles;
    }

    private static String stripPngExtension(Path filePath) {
        String path = filePath.toString();
        return path.endsWith(".png") ? path.substring(0, path.length() - 4) : path;
    }
}
