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

            paragraphs.add("### 模型参数一眼读懂");
            paragraphs.add(String.format(
                    "- 初始占比：鹰派 %.1f%%、鸽派 %.1f%%，复制强度 %.2f，突变率 %.2f。",
                    percentage(first.hawkShare()),
                    percentage(first.doveShare()),
                    parameters.selectionStrength(),
                    parameters.mutationRate()
            ));
            paragraphs.add(String.format(
                    "- 冲突成本 C=%.1f 远高于资源价值 V=%.1f，理论均衡鹰派占比约 %.1f%%。",
                    parameters.conflictCost(),
                    parameters.resourceValue(),
                    essPercent
            ));
            paragraphs.add(String.format(
                    "- 仿真输出 %d 代数据，包含鹰派/鸽派占比、策略收益与群体平均收益。",
                    result.generations().size()
            ));

            paragraphs.add("### 三个关键节点");
            paragraphs.add(String.format(
                    "- 鹰派占比 %.1f%% → 第 %d 代 %.1f%%，鸽派相应升至 %.1f%%，几乎贴近理论值。",
                    percentage(first.hawkShare()),
                    last.generation(),
                    percentage(last.hawkShare()),
                    percentage(last.doveShare())
            ));
            paragraphs.add(String.format(
                    "- 群体平均收益从 %.2f 回升到 %.2f，说明自损型冲突被快速纠偏。",
                    first.averagePayoff(),
                    last.averagePayoff()
            ));
            String stabilizationText = stabilizationGeneration >= 0
                    ? "第 " + stabilizationGeneration + " 代"
                    : "模拟范围内暂未";
            paragraphs.add(String.format(
                    "- %s 进入“±2%%”均衡窗口，混合策略成为演化稳定解。",
                    stabilizationText
            ));

            double hawkHawkPayoff = (parameters.resourceValue() - parameters.conflictCost()) / 2.0;
            paragraphs.add("### 给管理者的三条提醒");
            paragraphs.add(String.format(
                    "- 当冲突成本 %.1f 大于收益 %.1f，两败俱伤不是比喻，而是公式：鹰派对鹰派平均只剩 %.2f。",
                    parameters.conflictCost(),
                    parameters.resourceValue(),
                    hawkHawkPayoff
            ));
            paragraphs.add(String.format(
                    "- 想把团队从内耗里拽出来，就要安排人成为“鸽派缓冲区”，让占比逼近 %.1f%%。",
                    100.0 - essPercent
            ));
            paragraphs.add("- 留一点突变（约 1%）机制，能防止组织陷入单一激进态，适合写成“预留缓冲账户”的比喻。");

            paragraphs.add("### 写稿小贴士");
            paragraphs.add("- 先抛“价格战均衡点=V/C”这个公式，再贴鹰派占比折线，一句话就能解释企业为何需要停战。");
            paragraphs.add(String.format(
                    "- 用“第 %d 代回到正收益”作为情绪拐点，引导读者思考停止内耗的时间窗口。",
                    stabilizationGeneration >= 0 ? stabilizationGeneration : last.generation()
            ));
            paragraphs.add("- 结尾把模型代入行业案例（直播带货、房产促销），数据就不再是抽象数学。");
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
                            "鹰派与鸽派占比演化",
                            "代数",
                            "占比（%）",
                            List.of(
                                    ChartSeries.of("鹰派占比", generationIndex, hawkShareSeries),
                                    ChartSeries.of("鸽派占比", generationIndex, doveShareSeries),
                                    ChartSeries.of("理论均衡", generationIndex, essShareSeries)
                            )
                    ),
                    new ChartAttachment(
                            "strategy-payoff.png",
                            "策略收益对比",
                            "代数",
                            "平均收益",
                            List.of(
                                    ChartSeries.of("鹰派收益", generationIndex, hawkPayoffSeries),
                                    ChartSeries.of("鸽派收益", generationIndex, dovePayoffSeries)
                            )
                    ),
                    new ChartAttachment(
                            "average-payoff.png",
                            "群体平均收益",
                            "代数",
                            "平均收益",
                            List.of(ChartSeries.of("平均收益", generationIndex, averagePayoffSeries))
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
