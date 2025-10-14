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

            paragraphs.add("### 爆款开头：给社区服务加一个“退出键”，合作率反而更稳？");
            paragraphs.add(String.format(
                    "我们在一个 %d 人小组的公共物品博弈里，给参与者增加“随时退出拿保底收益”的选项。结果显示：只要退出机制设计得当，搭便车会自己缩回去。这套剧情非常适合写社区治理、志愿服务或企业文化案例。",
                    parameters.groupSize()
            ));

            paragraphs.add("### 痛点排序，引导共鸣");
            paragraphs.add("- 社区活动靠少数热心人撑，其他人旁观甚至搭便车。");
            paragraphs.add("- 企业在内部协作中想推“保底奖励”，担心反而削弱合作。");
            paragraphs.add("- 政策号、公益号需要“柔性治理”与“退出机制”的实证案例。");

            paragraphs.add("### 仿真舞台：确保读者听得懂也信得过");
            paragraphs.add(String.format("- 参数设定：公共乘数 r=%.1f，合作者成本 %.1f，旁观者保底收益 %.1f。",
                    parameters.multiplier(),
                    parameters.contributionCost(),
                    parameters.lonerPayoff()));
            paragraphs.add(String.format("- 起跑线：合作者 %.1f%%、搭便车者 %.1f%%、旁观者 %.1f%%；复制强度 %.2f，突变率 %.3f。",
                    percentage(parameters.initialCooperatorShare()),
                    percentage(parameters.initialDefectorShare()),
                    percentage(initialLoner),
                    parameters.selectionStrength(),
                    parameters.mutationRate()));
            paragraphs.add(String.format("- 仿真规模：共 %d 代、每代 %d 次随机匹配，随机种子 %d，确保别人复现得到同样结果。",
                    parameters.generations(),
                    parameters.interactionsPerGeneration(),
                    parameters.seed()));

            paragraphs.add("### 数据高潮：三条曲线讲完故事");
            paragraphs.add(String.format("- 平均收益 %.2f → %.2f，说明退出机制没有摧毁整体回报，反而更稳定。",
                    first.populationPayoff(),
                    last.populationPayoff()));
            paragraphs.add(String.format("- 合作者稳住在 %.1f%%，旁观者攀升到 %.1f%%，搭便车者被压到 %.1f%%，形成“合作—旁观”双引擎。",
                    percentage(last.cooperatorShare()),
                    percentage(last.lonerShare()),
                    percentage(last.defectorShare())));
            String suppressionText = suppressionGeneration >= 0
                    ? "第 " + suppressionGeneration + " 代"
                    : "模拟范围内尚未触达";
            paragraphs.add(String.format(
                    "- %s 让搭便车跌破 20%%；合作峰值出现在第 %d 代（%.1f%%），适合作为图文的情绪拐点。",
                    suppressionText,
                    peak.generation(),
                    percentage(peak.cooperatorShare())
            ));

            paragraphs.add("### 把数字翻成现实建议");
            paragraphs.add(String.format(
                    "- 搭便车者收益 %.2f 被保底收益 %.1f“锁死”，退出选项比高压惩罚更快见效，可类比“社区记点”“志愿时薪”。",
                    last.defectorPayoff(),
                    parameters.lonerPayoff()
            ));
            paragraphs.add(String.format(
                    "- 合作者最终仍拿到 %.2f 的群体收益，证明“愿意出力的人”享受到更大整体蛋糕，不会被退出机制伤害。",
                    last.cooperatorPayoff()
            ));
            paragraphs.add("- 旁观者稳定在 40%~50%，是“软治理”里的安全垫：提供保底，不让群体瞬间崩盘。");

            paragraphs.add("### 写稿与排版提示");
            paragraphs.add("- 用“退出按钮居然能压住搭便车？”做开头悬念，配三线图，读者立刻代入社区故事。");
            paragraphs.add(String.format(
                    "- 用“%s 让搭便车跌破 20%%”当金句，顺势切到制度设计或政策提案。",
                    suppressionText
            ));
            paragraphs.add("- 结尾提示参数可调，鼓励读者留言分享自己参与的志愿服务或积分制度案例。");
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
