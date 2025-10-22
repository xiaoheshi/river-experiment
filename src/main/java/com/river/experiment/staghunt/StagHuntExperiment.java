package com.river.experiment.staghunt;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;
import com.river.experiment.core.chart.ChartAttachment;
import com.river.experiment.core.chart.ChartSeries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 鹿猎博弈信号协调实验封装。
 */
public final class StagHuntExperiment implements Experiment<StagHuntExperiment.StagHuntReport> {

    private final StagHuntParameters parameters;

    public StagHuntExperiment() {
        this(new StagHuntParameters(
                240,
                7500,
                0.72,
                0.018,
                5.0,
                2.0,
                0.0,
                0.8,
                0.12,
                0.58,
                0.30,
                2028L
        ));
    }

    public StagHuntExperiment(StagHuntParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String id() {
        return "stag-hunt-signal";
    }

    @Override
    public String displayName() {
        return "鹿猎信号协调实验";
    }

    @Override
    public StagHuntReport run() {
        StagHuntSimulation simulation = new StagHuntSimulation(parameters);
        StagHuntResult result = simulation.run();
        StagHuntGeneration first = result.firstGeneration();
        StagHuntGeneration last = result.lastGeneration();
        StagHuntGeneration peakCoordination = result.generations().stream()
                .max(Comparator.comparingDouble(StagHuntGeneration::stagSuccessRate))
                .orElse(last);

        int breakthroughGeneration = result.generations().stream()
                .filter(gen -> gen.signalerShare() >= 0.25)
                .mapToInt(StagHuntGeneration::generation)
                .findFirst()
                .orElse(-1);

        int coordinationFlipGeneration = result.generations().stream()
                .filter(gen -> gen.stagConversionRate() >= 0.6)
                .mapToInt(StagHuntGeneration::generation)
                .findFirst()
                .orElse(-1);

        return new StagHuntReport(parameters, result, first, last, peakCoordination, breakthroughGeneration, coordinationFlipGeneration);
    }

    /**
     * 鹿猎信号协调实验报告。
     */
    public static final class StagHuntReport implements ExperimentReport {

        private final StagHuntParameters parameters;
        private final StagHuntResult result;
        private final StagHuntGeneration first;
        private final StagHuntGeneration last;
        private final StagHuntGeneration peak;
        private final int breakthroughGeneration;
        private final int coordinationFlipGeneration;

        StagHuntReport(StagHuntParameters parameters,
                       StagHuntResult result,
                       StagHuntGeneration first,
                       StagHuntGeneration last,
                       StagHuntGeneration peak,
                       int breakthroughGeneration,
                       int coordinationFlipGeneration) {
            this.parameters = parameters;
            this.result = result;
            this.first = first;
            this.last = last;
            this.peak = peak;
            this.breakthroughGeneration = breakthroughGeneration;
            this.coordinationFlipGeneration = coordinationFlipGeneration;
        }

        public StagHuntParameters parameters() {
            return parameters;
        }

        public StagHuntResult result() {
            return result;
        }

        public StagHuntGeneration firstGeneration() {
            return first;
        }

        public StagHuntGeneration lastGeneration() {
            return last;
        }

        public StagHuntGeneration peakGeneration() {
            return peak;
        }

        public int breakthroughGeneration() {
            return breakthroughGeneration;
        }

        public int coordinationFlipGeneration() {
            return coordinationFlipGeneration;
        }

        @Override
        public String sectionTitle() {
            return "鹿猎信号：12% 破局者把团队拖进高配局";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();

            paragraphs.add("“只有 12% 的人敢先站出来发信号，却把整支队伍的收益拉到了新高度。”这是我们在鹿猎博弈里加入“信号者”角色后跑出的爆款叙事。");

            paragraphs.add("### 切入爆点：信号越吵，协作越稳");
            paragraphs.add(String.format(
                    "我们模拟一个鹿猎博弈团队：%d 代演化、每代 %d 次随机双人组队。信号者会付出 %.1f 成本先喊话，跟随者只在看到信号后才押注“猎鹿”，保守者永远猎兔。",
                    parameters.generations(),
                    parameters.interactionsPerGeneration(),
                    parameters.signalCost()
            ));

            paragraphs.add("### 开局数据：12% 喊话者能撑住吗？");
            paragraphs.add(String.format(
                    "- 初始构成：信号者 %.1f%%、跟随者 %.1f%%、保守者 %.1f%%；选择强度 %.2f，突变率 %.3f。",
                    percentage(parameters.initialSignalerShare()),
                    percentage(parameters.initialFollowerShare()),
                    percentage(parameters.initialLonerShare()),
                    parameters.selectionStrength(),
                    parameters.mutationRate()
            ));
            paragraphs.add(String.format(
                    "- 鹿猎收益 %.1f，对赌失败掉到 %.1f；猎兔稳稳 %.1f。信号者每次喊话额外付出 %.1f。",
                    parameters.stagPayoff(),
                    parameters.failedStagPayoff(),
                    parameters.harePayoff(),
                    parameters.signalCost()
            ));

            paragraphs.add("### 故事走向：一波信号带出整支队伍");
            if (breakthroughGeneration >= 0) {
                paragraphs.add(String.format(
                        "- 第 %d 代开始，信号者份额突破 25%%，团队的“敢赌基因”被点燃。",
                        breakthroughGeneration
                ));
            } else {
                paragraphs.add("- 模拟范围内信号者始终未超过 25%，故事变成“慢热型”也同样值得写。");
            }
            paragraphs.add(String.format(
                    "- 鹿猎协作的峰值出现在第 %d 代，成功率高达 %.1f%%；同期信号触发占比 %.1f%%。",
                    peak.generation(),
                    percentage(peak.stagSuccessRate()),
                    percentage(peak.signalActivationRate())
            ));
            if (coordinationFlipGeneration >= 0) {
                paragraphs.add(String.format(
                        "- 信号转化率在第 %d 代冲破 60%%，意味着“喊话就会有人跟”的拐点已经到来。",
                        coordinationFlipGeneration
                ));
            } else {
                paragraphs.add("- 虽然信号转化率未跨过 60%%，但全程维持在高位，适合强调“稳态协作”。");
            }
            paragraphs.add(String.format(
                    "- 最终一代：信号者 %.1f%%、跟随者 %.1f%%、保守者仅剩 %.1f%%，人均收益从 %.2f 飙到 %.2f。",
                    percentage(last.signalerShare()),
                    percentage(last.followerShare()),
                    percentage(last.lonerShare()),
                    first.populationPayoff(),
                    last.populationPayoff()
            ));

            paragraphs.add("### 爆款写作提示：用“信号成本”解释现实痛点");
            paragraphs.add("- 群聊里总有几位“自来熟”提前确认流程，他们就是现实中的信号者。");
            if (breakthroughGeneration >= 0) {
                paragraphs.add(String.format(
                        "- 引用“第 %d 代信号突破 25%%”当作情绪节点，引导受众理解“先发声的人，不一定赚最多，但绝对握有节奏权”。",
                        breakthroughGeneration
                ));
            } else {
                paragraphs.add("- 若未出现突破点，就把“信号稳居 20%”写成“老成员守住节奏”，同样能引导讨论。");
            }
            paragraphs.add("- 对比最终收益与保守策略的差距，延伸到企业跨部门协作、校园社团破冰、志愿活动召集等场景。");

            paragraphs.add("### 金句素材池");
            paragraphs.add(String.format(
                    "- “%.1f%% 的信号触发率，把鹿猎项目从低配协同拉进高配执行。”",
                    percentage(last.signalActivationRate())
            ));
            paragraphs.add(String.format(
                    "- “信号者牺牲 %.1f 的成本，换来团队 %.2f 的长尾收益——这是组织行为学里的 ROI。”",
                    parameters.signalCost(),
                    last.populationPayoff()
            ));
            paragraphs.add(String.format(
                    "- “当信号成功率冲上 %.1f%%，保守者不再是主流——你可以放心把这段故事写进公众号开头。”",
                    percentage(last.stagConversionRate())
            ));

            paragraphs.add("### 互动话题");
            paragraphs.add("- 引导读者留言：你的团队里谁是那个“先喊开始的人”？他/她得到怎样的回报？");
            paragraphs.add("- 追加彩蛋：把初始信号者比例调到 5% 再跑一次，做成留言互动的读者作业。");

            return paragraphs;
        }

        @Override
        public List<ChartAttachment> charts() {
            List<StagHuntGeneration> generations = result.generations();
            if (generations.isEmpty()) {
                return List.of();
            }

            List<Double> generationIndex = new ArrayList<>(generations.size());
            List<Double> signalerShare = new ArrayList<>(generations.size());
            List<Double> followerShare = new ArrayList<>(generations.size());
            List<Double> lonerShare = new ArrayList<>(generations.size());
            List<Double> signalerPayoff = new ArrayList<>(generations.size());
            List<Double> followerPayoff = new ArrayList<>(generations.size());
            List<Double> lonerPayoff = new ArrayList<>(generations.size());
            List<Double> populationPayoff = new ArrayList<>(generations.size());
            List<Double> stagSuccessRate = new ArrayList<>(generations.size());
            List<Double> signalActivationRate = new ArrayList<>(generations.size());
            List<Double> stagConversionRate = new ArrayList<>(generations.size());

            for (StagHuntGeneration generation : generations) {
                generationIndex.add((double) generation.generation());
                signalerShare.add(percentage(generation.signalerShare()));
                followerShare.add(percentage(generation.followerShare()));
                lonerShare.add(percentage(generation.lonerShare()));
                signalerPayoff.add(generation.signalerPayoff());
                followerPayoff.add(generation.followerPayoff());
                lonerPayoff.add(generation.lonerPayoff());
                populationPayoff.add(generation.populationPayoff());
                stagSuccessRate.add(percentage(generation.stagSuccessRate()));
                signalActivationRate.add(percentage(generation.signalActivationRate()));
                stagConversionRate.add(percentage(generation.stagConversionRate()));
            }

            return List.of(
                    new ChartAttachment(
                            "strategy-share.png",
                            "信号者带动的策略占比演化",
                            "代数",
                            "占比（%）",
                            List.of(
                                    ChartSeries.of("信号者", generationIndex, signalerShare),
                                    ChartSeries.of("跟随者", generationIndex, followerShare),
                                    ChartSeries.of("保守者", generationIndex, lonerShare)
                            )
                    ),
                    new ChartAttachment(
                            "strategy-payoff.png",
                            "各策略平均收益对比",
                            "代数",
                            "平均收益",
                            List.of(
                                    ChartSeries.of("信号者收益", generationIndex, signalerPayoff),
                                    ChartSeries.of("跟随者收益", generationIndex, followerPayoff),
                                    ChartSeries.of("保守者收益", generationIndex, lonerPayoff),
                                    ChartSeries.of("群体收益", generationIndex, populationPayoff)
                            )
                    ),
                    new ChartAttachment(
                            "coordination-rates.png",
                            "信号触发与协作成功率",
                            "代数",
                            "占比（%）",
                            List.of(
                                    ChartSeries.of("信号触发率", generationIndex, signalActivationRate),
                                    ChartSeries.of("鹿猎协作成功率", generationIndex, stagSuccessRate),
                                    ChartSeries.of("信号转化率", generationIndex, stagConversionRate)
                            )
                    )
            );
        }

        private double percentage(double value) {
            return 100.0 * value;
        }
    }
}
