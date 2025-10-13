package com.river.experiment.hawkdove;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;

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

        @Override
        public String sectionTitle() {
            return "鹰鸽冲突：资源竞争下的混合均衡";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            paragraphs.add(String.format(
                    "实验设定：资源价值 V=%.1f，争夺冲突成本 C=%.1f，初始鹰派占比 %.1f%%，复制强度 %.2f，突变率 %.2f；理论上鹰派均衡点在 V/C=%.1f%%。",
                    parameters.resourceValue(),
                    parameters.conflictCost(),
                    percentage(first.hawkShare()),
                    parameters.selectionStrength(),
                    parameters.mutationRate(),
                    percentage(essShare)
            ));
            paragraphs.add(String.format(
                    "动态结果：第 0 代鹰派 %.1f%% → 第 %d 代 %.1f%%，鸽派则从 %.1f%% → %.1f%%，平均收益由 %.2f 收敛至 %.2f。首次进入均衡窗口（±2%%）的代数：%s。",
                    percentage(first.hawkShare()),
                    last.generation(),
                    percentage(last.hawkShare()),
                    percentage(first.doveShare()),
                    percentage(last.doveShare()),
                    first.averagePayoff(),
                    last.averagePayoff(),
                    stabilizationGeneration >= 0 ? ("第 " + stabilizationGeneration + " 代") : "模拟范围内未收敛"
            ));
            paragraphs.add("解读要点：");
            paragraphs.add(String.format(
                    "· 由于 C≫V，鹰派之间的冲突收益为 %.2f，远低于与鸽派互动时的 %.2f，促使鹰派占比下降。",
                    (parameters.resourceValue() - parameters.conflictCost()) / 2.0,
                    parameters.resourceValue()
            ));
            paragraphs.add(String.format(
                    "· 复制器动力学在约第 %d 代后围绕理论均衡 %.1f%% 振荡，说明混合策略是演化稳定策略（ESS）。",
                    stabilizationGeneration >= 0 ? stabilizationGeneration : last.generation(),
                    percentage(essShare)
            ));
            paragraphs.add("· 保留少量突变（1%）避免陷入边界解，便于观察均衡附近的自然波动，可直接绘制鹰派占比折线用于讲解。");
            return paragraphs;
        }

        private double percentage(double value) {
            return 100.0 * value;
        }

        public SimulationResult result() {
            return result;
        }
    }
}
