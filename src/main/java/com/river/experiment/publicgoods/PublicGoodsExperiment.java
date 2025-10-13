package com.river.experiment.publicgoods;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;

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

        @Override
        public String sectionTitle() {
            return "公共物品博弈：合作、搭便车与退出的动态平衡";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            double initialLoner = parameters.initialLonerShare();
            paragraphs.add(String.format(
                    "实验设定：每组 %d 人，公共物品乘数 r=%.1f，合作者成本 %.1f，旁观者保障收益 %.1f；初始占比——合作者 %.1f%%、搭便车者 %.1f%%、旁观者 %.1f%%，复制强度 %.2f，突变率 %.2f。",
                    parameters.groupSize(),
                    parameters.multiplier(),
                    parameters.contributionCost(),
                    parameters.lonerPayoff(),
                    percentage(parameters.initialCooperatorShare()),
                    percentage(parameters.initialDefectorShare()),
                    percentage(initialLoner),
                    parameters.selectionStrength(),
                    parameters.mutationRate()
            ));
            paragraphs.add(String.format(
                    "动态结果：平均收益由 %.2f 上升至 %.2f；合作者从 %.1f%% 增至 %.1f%%，搭便车者降至 %.1f%%，旁观者收敛到 %.1f%%。最高合作度出现在第 %d 代（%.1f%%），首次将搭便车比例压到 20%% 以下的代数：%s。",
                    first.populationPayoff(),
                    last.populationPayoff(),
                    percentage(first.cooperatorShare()),
                    percentage(last.cooperatorShare()),
                    percentage(last.defectorShare()),
                    percentage(last.lonerShare()),
                    peak.generation(),
                    percentage(peak.cooperatorShare()),
                    suppressionGeneration >= 0 ? ("第 " + suppressionGeneration + " 代") : "模拟范围内未达成"
            ));
            paragraphs.add("解读要点：");
            paragraphs.add(String.format(
                    "· 搭便车者在参与群体中获得 %.2f 的平均收益，虽免除成本但被旁观者的保底收益 %.2f 部分取代，持续下降到 %.1f%%。",
                    last.defectorPayoff(),
                    parameters.lonerPayoff(),
                    percentage(last.defectorShare())
            ));
            paragraphs.add(String.format(
                    "· 合作者在多数群体里获得 %.2f 的群体收益，扣除成本后仍高于旁观者，促使合作率稳态约 %.1f%%。",
                    last.cooperatorPayoff(),
                    percentage(last.cooperatorShare())
            ));
            paragraphs.add("· 旁观者提供“退出选项”，在噪声与突变下维持少量比例，缓冲搭便车者的扩散，可用于演示制度化惩罚之外的自组织机制。");
            return paragraphs;
        }

        private double percentage(double value) {
            return 100.0 * value;
        }

        public PublicGoodsResult result() {
            return result;
        }
    }
}
