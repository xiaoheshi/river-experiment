package com.river.experiment.core.chart;

import java.util.List;
import java.util.Objects;

/**
 * 图表输出描述，包含生成 PNG 所需的基本信息。
 */
public record ChartAttachment(
        String fileName,
        String title,
        String xAxisLabel,
        String yAxisLabel,
        List<ChartSeries> series) {

    public ChartAttachment {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(xAxisLabel, "xAxisLabel");
        Objects.requireNonNull(yAxisLabel, "yAxisLabel");
        Objects.requireNonNull(series, "series");
        if (series.isEmpty()) {
            throw new IllegalArgumentException("series must not be empty");
        }
    }
}
