package com.river.experiment.core;

/**
 * 通用实验接口，每个实验包需提供实现。
 *
 * @param <R> 实验产出的报告类型
 */
public interface Experiment<R extends ExperimentReport> {

    /**
     * 返回实验的唯一标识（用于链接或配置）。
     */
    String id();

    /**
     * 返回实验的中文显示名称。
     */
    String displayName();

    /**
     * 运行实验并返回报告。
     */
    R run();
}
