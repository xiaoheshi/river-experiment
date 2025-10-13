package com.river.experiment.core.article;

import java.nio.file.Path;
import java.util.List;

/**
 * 记录导出的 Markdown 文章路径与关联图表资源。
 */
public record ArticleExportResult(Path articlePath, List<Path> chartFiles) {
}
