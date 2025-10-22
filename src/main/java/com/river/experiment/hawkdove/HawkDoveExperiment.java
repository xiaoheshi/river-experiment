package com.river.experiment.hawkdove;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;
import com.river.experiment.core.chart.ChartAttachment;
import com.river.experiment.core.chart.ChartSeries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 鹰鸽博弈的封装实验。
 */
public final class HawkDoveExperiment implements Experiment<HawkDoveExperiment.HawkDoveReport> {

    private final SimulationParameters parameters;

    public HawkDoveExperiment() {
        this(new SimulationParameters(
                180,
                0.72,
                2.0,
                6.0,
                0.01,
                0.5
        ));
    }

    public HawkDoveExperiment(SimulationParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public String id() {
        return "hawk-dove";
    }

    @Override
    public String displayName() {
        return "鹰鸽冲突实验";
    }

    @Override
    public HawkDoveReport run() {
        HawkDoveSimulation simulation = new HawkDoveSimulation(parameters);
        SimulationResult result = simulation.run();
        GenerationState first = result.firstGeneration();
        GenerationState last = result.lastGeneration();
        double essShare = Math.max(0.0, Math.min(1.0, parameters.resourceValue() / parameters.conflictCost()));

        int stabilizationGeneration = result.generations().stream()
                .filter(state -> Math.abs(state.hawkShare() - essShare) <= 0.02)
                .map(GenerationState::generation)
                .min(Comparator.naturalOrder())
                .orElse(-1);

        return new HawkDoveReport(parameters, result, first, last, essShare, stabilizationGeneration);
    }

    /**
     * 鹰鸽博弈的结果报告。
     */
    public static final class HawkDoveReport implements ExperimentReport {

        private final SimulationParameters parameters;
        private final SimulationResult result;
        private final GenerationState first;
        private final GenerationState last;
        private final double essShare;
        private final int stabilizationGeneration;

        HawkDoveReport(SimulationParameters parameters,
                       SimulationResult result,
                       GenerationState first,
                       GenerationState last,
                       double essShare,
                       int stabilizationGeneration) {
            this.parameters = parameters;
            this.result = result;
            this.first = first;
            this.last = last;
            this.essShare = essShare;
            this.stabilizationGeneration = stabilizationGeneration;
        }

        public SimulationParameters parameters() {
            return parameters;
        }

        public double essShare() {
            return essShare;
        }

        public int stabilizationGeneration() {
            return stabilizationGeneration;
        }

        @Override
        public String sectionTitle() {
            return "价格战越打越穷：鹰鸽模型的混合均衡警报";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            double essPercent = percentage(essShare);
            paragraphs.add(String.format(
                    "价格战为什么总是“赢了面子亏了钱”？我们让资源价值 V=%.1f、冲突成本 C=%.1f 的鹰鸽模型跑了 %d 代，记录混合均衡如何自动出现。",
                    parameters.resourceValue(),
                    parameters.conflictCost(),
                    last.generation()
            ));

            paragraphs.add("### 爆款开头：价格战打到骨折，谁先喊停？");
            paragraphs.add(String.format(
                    "把一场资源价值只有 %.1f，却要付出 %.1f 冲突成本的价格战放进演化模型，让“鹰派 vs 鸽派”的真实动态自己跑出来。只要你在电商、地产、MCN 或职场竞争里被内卷过，这段数据就能戳中你。",
                    parameters.resourceValue(),
                    parameters.conflictCost()
            ));

            paragraphs.add("### 读者痛点切入");
            paragraphs.add("- 促销大战越打越亏，到底什么时候该停？");
            paragraphs.add("- 团队内部竞争升级，如何避免“鱼死网破”？");
            paragraphs.add("- 企业想做“让利”宣传，可否拿到可视化证据？");

            paragraphs.add("### 仿真舞台搭建细节");
            paragraphs.add(String.format(
                    "- 初始态：鹰派 %.1f%%、鸽派 %.1f%%，复制强度 %.2f，突变率 %.2f，保留少量随机扰动防止陷入死局。",
                    percentage(first.hawkShare()),
                    percentage(first.doveShare()),
                    parameters.selectionStrength(),
                    parameters.mutationRate()
            ));
            paragraphs.add(String.format(
                    "- 成本结构：资源价值 V=%.1f vs 冲突成本 C=%.1f，理论均衡鹰派占比 %.1f%%，让“赢家也可能输光”成为必然。",
                    parameters.resourceValue(),
                    parameters.conflictCost(),
                    essPercent
            ));
            paragraphs.add(String.format(
                    "- 输出：%d 代数据（占比、策略收益、群体收益），方便制作折线、双轴或瀑布图。",
                    result.generations().size()
            ));

            paragraphs.add("### 数据高潮三连击");
            paragraphs.add(String.format(
                    "- 鹰派占比 %.1f%% → 第 %d 代 %.1f%%，鸽派升至 %.1f%%，精准贴合理论均衡。",
                    percentage(first.hawkShare()),
                    last.generation(),
                    percentage(last.hawkShare()),
                    percentage(last.doveShare())
            ));
            paragraphs.add(String.format(
                    "- 群体平均收益从 %.2f 回到 %.2f，说明“暂停内耗”能迅速让组织回血。",
                    first.averagePayoff(),
                    last.averagePayoff()
            ));
            String stabilizationText = stabilizationGeneration >= 0
                    ? "第 " + stabilizationGeneration + " 代"
                    : "模拟范围内暂未";
            paragraphs.add(String.format(
                    "- %s 进入“±2%%”均衡窗口，混合策略正式成为演化稳定解，可做成“价格战停火时间线”。",
                    stabilizationText
            ));

            double hawkHawkPayoff = (parameters.resourceValue() - parameters.conflictCost()) / 2.0;
            paragraphs.add("### 给操盘手和管理者的提醒");
            paragraphs.add(String.format(
                    "- 当冲突成本 %.1f 大于收益 %.1f，“两败俱伤”不再是形容词：鹰派互殴平均只剩 %.2f。",
                    parameters.conflictCost(),
                    parameters.resourceValue(),
                    hawkHawkPayoff
            ));
            paragraphs.add(String.format(
                    "- 组织需要保留 %.1f%% 左右的鸽派缓冲区，他们是停战与谈判的“安全阀”。",
                    100.0 - essPercent
            ));
            paragraphs.add("- 留下约 1% 的策略突变空间，可以类比为“预留缓冲资金/试点团队”，防止集体冲向毁灭。");

            paragraphs.add("### 写稿与传播提示");
            paragraphs.add("- 开头用“价格战均衡点=V/C”配数据图，瞬间把抽象理论说成人话。");
            paragraphs.add(String.format(
                    "- 用“第 %d 代回到正收益”做情绪拐点，配上企业案例或职场故事，增强代入感。",
                    stabilizationGeneration >= 0 ? stabilizationGeneration : last.generation()
            ));
            paragraphs.add("- 结尾把行业案例（直播带货、房产促销、红包大战）与模型对照，加一句“你们行业的均衡点是多少？”引导留言。");
            return paragraphs;
        }

        @Override
        public List<ChartAttachment> charts() {
            List<GenerationState> generations = result.generations();
            if (generations.isEmpty()) {
                return List.of();
            }

            List<Double> generationIndex = new ArrayList<>(generations.size());
            List<Double> hawkShareSeries = new ArrayList<>(generations.size());
            List<Double> doveShareSeries = new ArrayList<>(generations.size());
            List<Double> essShareSeries = new ArrayList<>(generations.size());
            List<Double> hawkPayoffSeries = new ArrayList<>(generations.size());
            List<Double> dovePayoffSeries = new ArrayList<>(generations.size());
            List<Double> averagePayoffSeries = new ArrayList<>(generations.size());

            for (GenerationState state : generations) {
                generationIndex.add((double) state.generation());
                hawkShareSeries.add(percentage(state.hawkShare()));
                doveShareSeries.add(percentage(state.doveShare()));
                essShareSeries.add(percentage(essShare));
                hawkPayoffSeries.add(state.hawkPayoff());
                dovePayoffSeries.add(state.dovePayoff());
                averagePayoffSeries.add(state.averagePayoff());
            }

            return List.of(
                    new ChartAttachment(
                            "strategy-share.png",
                            "Strategy Share Over Generations",
                            "Generation",
                            "Share (%)",
                            List.of(
                                    ChartSeries.of("Hawk Share", generationIndex, hawkShareSeries),
                                    ChartSeries.of("Dove Share", generationIndex, doveShareSeries),
                                    ChartSeries.of("Theoretical ESS", generationIndex, essShareSeries)
                            )
                    ),
                    new ChartAttachment(
                            "strategy-payoff.png",
                            "Average Payoff by Strategy",
                            "Generation",
                            "Average Payoff",
                            List.of(
                                    ChartSeries.of("Hawk Payoff", generationIndex, hawkPayoffSeries),
                                    ChartSeries.of("Dove Payoff", generationIndex, dovePayoffSeries)
                            )
                    ),
                    new ChartAttachment(
                            "average-payoff.png",
                            "Population Average Payoff",
                            "Generation",
                            "Average Payoff",
                            List.of(ChartSeries.of("Average Payoff", generationIndex, averagePayoffSeries))
                    )
            );
        }

        private double percentage(double value) {
            return 100.0 * value;
        }

        public SimulationResult result() {
            return result;
        }
    }
}
