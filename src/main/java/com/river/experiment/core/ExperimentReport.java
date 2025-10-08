package com.river.experiment.core;

import java.util.List;

/**
 * 统一的实验报告接口，便于统一排版输出公众号文章内容。
 */
public interface ExperimentReport {

    /**
     * 中文章节标题。
     */
    String sectionTitle();

    /**
     * 章节正文段落集合，按顺序输出。
     */
    List<String> paragraphs();
}
