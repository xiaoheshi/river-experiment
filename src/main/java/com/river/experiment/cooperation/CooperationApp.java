package com.river.experiment.cooperation;

import java.nio.file.Path;
import java.util.List;

import com.river.experiment.cooperation.CooperationExperiment.CooperationReport;
import com.river.experiment.core.article.ArticleExportResult;
import com.river.experiment.core.article.MarkdownArticleWriter;

/**
 * 协同进化实验独立入口。
 */
public final class CooperationApp {

    private static final int TOP_N = 5;

    public static void main(String[] args) {
        CooperationExperiment experiment = new CooperationExperiment();
        CooperationReport report = experiment.run();

        System.out.println("【协同的进化】" + report.sectionTitle());
        printTopStrategies(report);
        printTopAgents(report);

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

    private static void printTopStrategies(CooperationReport report) {
        List<StrategyPerformance> strategies = report.tournamentResult().strategyPerformances();
        int limit = Math.min(TOP_N, strategies.size());
        if (limit == 0) {
            System.out.println("暂无策略统计。");
            return;
        }
        System.out.println("策略综合排名（前 " + limit + "）：");
        for (int i = 0; i < limit; i++) {
            StrategyPerformance performance = strategies.get(i);
            System.out.printf("  第 %d 名 %s —— 场均得分 %.3f，合作率 %.1f%%%n",
                    i + 1,
                    performance.strategy().displayName(),
                    performance.meanScorePerRound(report.settings().rounds()),
                    performance.meanCooperationRate() * 100);
        }
    }

    private static void printTopAgents(CooperationReport report) {
        List<AgentPerformance> agents = report.tournamentResult().agentPerformances();
        int limit = Math.min(TOP_N, agents.size());
        if (limit == 0) {
            System.out.println("暂无角色统计。");
            return;
        }
        System.out.println("角色个人排名（前 " + limit + "）：");
        for (int i = 0; i < limit; i++) {
            AgentPerformance performance = agents.get(i);
            System.out.printf("  第 %d 名 %s（%s） —— 累积得分 %.2f，合作率 %.1f%%%n",
                    i + 1,
                    performance.agentId(),
                    performance.strategy().displayName(),
                    performance.meanScore(),
                    performance.meanCooperationRate() * 100);
        }
    }

    private CooperationApp() {
    }
}
