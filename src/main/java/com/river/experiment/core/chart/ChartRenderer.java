package com.river.experiment.core.chart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.awt.Color;
import java.text.DecimalFormat;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.AnnotationText;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.Styler;

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
            styleChart(chart);

            attachment.series().forEach(series -> addSeriesWithHighlights(chart, series));

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

    private static void styleChart(XYChart chart) {
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(new Color(248, 249, 252));
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setLegendBorderColor(new Color(220, 223, 230));
        chart.getStyler().setLegendSeriesLineLength(32);
        chart.getStyler().setMarkerSize(6);
        chart.getStyler().setSeriesColors(new Color[]{
                new Color(66, 133, 244),
                new Color(219, 68, 55),
                new Color(244, 180, 0),
                new Color(15, 157, 88),
                new Color(171, 71, 188),
                new Color(0, 172, 193)
        });
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setXAxisTitleVisible(true);
        chart.getStyler().setYAxisTitleVisible(true);
        chart.getStyler().setXAxisLabelRotation(0);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridLinesColor(new Color(226, 232, 240));
        chart.getStyler().setPlotGridHorizontalLinesVisible(true);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);
        chart.getStyler().setAxisTickMarksColor(new Color(189, 197, 209));
        chart.getStyler().setXAxisDecimalPattern("###,###.##");
        chart.getStyler().setYAxisDecimalPattern("###,###.##");
        chart.getStyler().setPlotContentSize(0.92);
    }

    private static void addSeriesWithHighlights(XYChart chart, ChartSeries series) {
        var addedSeries = chart.addSeries(series.name(), series.xValues(), series.yValues());
        addedSeries.setMarker(SeriesMarkers.CIRCLE);

        if (series.xValues().length == 0 || series.yValues().length == 0) {
            return;
        }

        // 起点注释
        double startX = series.xValues()[0];
        double startY = series.yValues()[0];
        chart.addAnnotation(createAnnotation("起点 " + formatValue(startY), startX, startY, true));

        // 峰值注释
        int maxIndex = maxIndex(series.yValues());
        double maxX = series.xValues()[maxIndex];
        double maxY = series.yValues()[maxIndex];
        chart.addAnnotation(createAnnotation("峰值 " + formatValue(maxY), maxX, maxY, false));

        // 终点注释（若不同于峰值）
        double endX = series.xValues()[series.xValues().length - 1];
        double endY = series.yValues()[series.yValues().length - 1];
        if (Math.abs(endX - maxX) > 1e-6 || Math.abs(endY - maxY) > 1e-6) {
            chart.addAnnotation(createAnnotation("最新 " + formatValue(endY), endX, endY, false));
        }
    }

    private static int maxIndex(double[] values) {
        double maxValue = Double.NEGATIVE_INFINITY;
        int maxIndex = 0;
        for (int i = 0; i < values.length; i++) {
            double value = values[i];
            if (value > maxValue) {
                maxValue = value;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static String formatValue(double value) {
        double abs = Math.abs(value);
        DecimalFormat format;
        if (abs >= 100) {
            format = new DecimalFormat("###,###");
        } else if (abs >= 10) {
            format = new DecimalFormat("###,##0.0");
        } else {
            format = new DecimalFormat("0.00");
        }
        return format.format(value);
    }

    private static AnnotationText createAnnotation(String text, double x, double y, boolean pushUpwards) {
        double offset = Math.max(Math.abs(y) * 0.04, 0.02);
        double adjustedY = pushUpwards ? y + offset : y - offset;
        return new AnnotationText(text, x, adjustedY, false);
    }
}
