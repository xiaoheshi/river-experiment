package com.river.experiment.core.chart;

import java.util.List;
import java.util.Objects;

/**
 * 单个序列的数据描述，用于绘制折线或散点图。
 */
public record ChartSeries(String name, double[] xValues, double[] yValues) {

    public ChartSeries {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(xValues, "xValues");
        Objects.requireNonNull(yValues, "yValues");
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("xValues and yValues must have the same length");
        }
    }

    /**
     * 创建序列并将集合转换为基础数组，便于与绘图库配合。
     */
    public static ChartSeries of(String name, List<Double> xValues, List<Double> yValues) {
        Objects.requireNonNull(xValues, "xValues");
        Objects.requireNonNull(yValues, "yValues");
        if (xValues.size() != yValues.size()) {
            throw new IllegalArgumentException("xValues and yValues must have the same length");
        }
        double[] xs = xValues.stream().mapToDouble(Double::doubleValue).toArray();
        double[] ys = yValues.stream().mapToDouble(Double::doubleValue).toArray();
        return new ChartSeries(name, xs, ys);
    }
}
