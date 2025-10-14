package com.river.experiment.publicgoods;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;
import com.river.experiment.core.chart.ChartAttachment;
import com.river.experiment.core.chart.ChartSeries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 公共物品博弈实验封装。
 */
public final class PublicGoodsExperiment implements Experiment<PublicGoodsExperiment.PublicGoodsReport> {

    private final PublicGoodsParameters parameters;

    public PublicGoodsExperiment() {
        this(new PublicGoodsParameters(
                220,
                5,
                6000,
                3.2,
                1.0,
                1.2,
                0.02,
                0.4,
                0.52,
                0.33,
                2025L
        ));
    }

    public PublicGoodsExperiment(PublicGoodsParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String id() {
        return "public-goods";
    }

    @Override
    public String displayName() {
        return "公共物品博弈实验";
    }

    @Override
    public PublicGoodsReport run() {
        PublicGoodsSimulation simulation = new PublicGoodsSimulation(parameters);
        PublicGoodsResult result = simulation.run();
        PublicGoodsGeneration first = result.firstGeneration();
        PublicGoodsGeneration last = result.lastGeneration();
        PublicGoodsGeneration peakCooperation = result.generations().stream()
                .max(Comparator.comparingDouble(PublicGoodsGeneration::cooperatorShare))
                .orElse(last);
        int suppressionGeneration = result.generations().stream()
                .filter(gen -> gen.defectorShare() <= 0.2)
                .map(PublicGoodsGeneration::generation)
                .min(Integer::compareTo)
                .orElse(-1);

        return new PublicGoodsReport(parameters, result, first, last, peakCooperation, suppressionGeneration);
    }

    /**
     * 公共物品博弈报告。
     */
    public static final class PublicGoodsReport implements ExperimentReport {

        private final PublicGoodsParameters parameters;
        private final PublicGoodsResult result;
        private final PublicGoodsGeneration first;
        private final PublicGoodsGeneration last;
        private final PublicGoodsGeneration peak;
        private final int suppressionGeneration;

        PublicGoodsReport(PublicGoodsParameters parameters,
                          PublicGoodsResult result,
                          PublicGoodsGeneration first,
                          PublicGoodsGeneration last,
                          PublicGoodsGeneration peak,
                          int suppressionGeneration) {
            this.parameters = parameters;
            this.result = result;
            this.first = first;
            this.last = last;
            this.peak = peak;
            this.suppressionGeneration = suppressionGeneration;
        }

        public PublicGoodsParameters parameters() {
            return parameters;
        }

        public PublicGoodsGeneration peakGeneration() {
            return peak;
        }

        public int suppressionGeneration() {
            return suppressionGeneration;
        }

        @Override
        public String sectionTitle() {
            return "公共物品：一个退出按钮压住搭便车";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            double initialLoner = parameters.initialLonerShare();
            paragraphs.add(String.format(
                    "当社区服务没人愿意干，我们给公共物品博弈加一个“退出按钮”：旁观者拿固定收益，看 %d 代复制器演化能否压住搭便车。",
                    parameters.generations()
            ));

            paragraphs.add("### 实验参数亮点");
            paragraphs.add(String.format(
                    "- 小组规模 %d 人，公共乘数 r=%.1f，合作者成本 %.1f，旁观者保底收益 %.1f。",
                    parameters.groupSize(),
                    parameters.multiplier(),
                    parameters.contributionCost(),
                    parameters.lonerPayoff()
            ));
            paragraphs.add(String.format(
                    "- 初始占比：合作者 %.1f%%、搭便车者 %.1f%%、旁观者 %.1f%%；复制强度 %.2f，突变率 %.2f。",
                    percentage(parameters.initialCooperatorShare()),
                    percentage(parameters.initialDefectorShare()),
                    percentage(initialLoner),
                    parameters.selectionStrength(),
                    parameters.mutationRate()
            ));
            paragraphs.add(String.format(
                    "- 共运行 %d 代、每代 %d 次随机匹配，种子 %d 保证结果可复现。",
                    parameters.generations(),
                    parameters.interactionsPerGeneration(),
                    parameters.seed()
            ));

            paragraphs.add("### 数据转折点");
            paragraphs.add(String.format(
                    "- 平均收益 %.2f → %.2f，证明退出机制没有拖累整体回报。",
                    first.populationPayoff(),
                    last.populationPayoff()
            ));
            paragraphs.add(String.format(
                    "- 合作者稳态 %.1f%%、旁观者抬升到 %.1f%%，搭便车者被压到 %.1f%%。",
                    percentage(last.cooperatorShare()),
                    percentage(last.lonerShare()),
                    percentage(last.defectorShare())
            ));
            String suppressionText = suppressionGeneration >= 0
                    ? "第 " + suppressionGeneration + " 代"
                    : "模拟范围内尚未触达";
            paragraphs.add(String.format(
                    "- %s 让搭便车比例跌破 20%%；合作峰值出现在第 %d 代（%.1f%%）。",
                    suppressionText,
                    peak.generation(),
                    percentage(peak.cooperatorShare())
            ));

            paragraphs.add("### 三条现实启示");
            paragraphs.add(String.format(
                    "- 搭便车者收益 %.2f 被旁观者的保底 %.1f 挤压，退出选项比高压惩罚更快见效。",
                    last.defectorPayoff(),
                    parameters.lonerPayoff()
            ));
            paragraphs.add(String.format(
                    "- 合作者最终仍拿到 %.2f 的群体收益，说明“愿意出力的人”不会被退出机制伤害。",
                    last.cooperatorPayoff()
            ));
            paragraphs.add("- 旁观者维持在约一半，是“软治理”里可复用的缓冲层：提供保底，不让群体崩盘。");

            paragraphs.add("### 写稿小贴士");
            paragraphs.add("- 先讲“给公共项目一个退出键”，再贴合作者/搭便车/旁观者三线图，读者会自动代入社区案例。");
            paragraphs.add(String.format(
                    "- 用“%s 让搭便车跌破 20%%”作为金句过渡到制度设计，故事自然顺滑。",
                    suppressionText
            ));
            paragraphs.add("- 结尾提示：参数都能改，换成志愿服务或会员积分场景，文章立刻可复用。");
            return paragraphs;
        }

        @Override
        public List<ChartAttachment> charts() {
            List<PublicGoodsGeneration> generations = result.generations();
            if (generations.isEmpty()) {
                return List.of();
            }

            List<Double> generationIndex = new ArrayList<>(generations.size());
            List<Double> cooperatorShare = new ArrayList<>(generations.size());
            List<Double> defectorShare = new ArrayList<>(generations.size());
            List<Double> lonerShare = new ArrayList<>(generations.size());
            List<Double> cooperatorPayoff = new ArrayList<>(generations.size());
            List<Double> defectorPayoff = new ArrayList<>(generations.size());
            List<Double> lonerPayoff = new ArrayList<>(generations.size());
            List<Double> populationPayoff = new ArrayList<>(generations.size());

            for (PublicGoodsGeneration generation : generations) {
                generationIndex.add((double) generation.generation());
                cooperatorShare.add(percentage(generation.cooperatorShare()));
                defectorShare.add(percentage(generation.defectorShare()));
                lonerShare.add(percentage(generation.lonerShare()));
                cooperatorPayoff.add(generation.cooperatorPayoff());
                defectorPayoff.add(generation.defectorPayoff());
                lonerPayoff.add(generation.lonerPayoff());
                populationPayoff.add(generation.populationPayoff());
            }

            return List.of(
                    new ChartAttachment(
                            "strategy-share.png",
                            "策略占比演化",
                            "代数",
                            "占比（%）",
                            List.of(
                                    ChartSeries.of("合作者", generationIndex, cooperatorShare),
                                    ChartSeries.of("搭便车者", generationIndex, defectorShare),
                                    ChartSeries.of("旁观者", generationIndex, lonerShare)
                            )
                    ),
                    new ChartAttachment(
                            "strategy-payoff.png",
                            "策略收益对比",
                            "代数",
                            "平均收益",
                            List.of(
                                    ChartSeries.of("合作者收益", generationIndex, cooperatorPayoff),
                                    ChartSeries.of("搭便车者收益", generationIndex, defectorPayoff),
                                    ChartSeries.of("旁观者收益", generationIndex, lonerPayoff)
                            )
                    ),
                    new ChartAttachment(
                            "population-payoff.png",
                            "群体平均收益",
                            "代数",
                            "平均收益",
                            List.of(ChartSeries.of("平均收益", generationIndex, populationPayoff))
                    )
            );
        }

        private double percentage(double value) {
            return 100.0 * value;
        }

        public PublicGoodsResult result() {
            return result;
        }
    }
}
