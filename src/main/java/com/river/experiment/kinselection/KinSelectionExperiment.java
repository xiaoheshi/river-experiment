package com.river.experiment.kinselection;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;
import com.river.experiment.core.chart.ChartAttachment;
import com.river.experiment.core.chart.ChartSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * 亲缘选择实验封装，独立于其他实验存在于单独的包中。
 */
public final class KinSelectionExperiment implements Experiment<KinSelectionExperiment.KinSelectionReport> {

    private final SimulationParameters parameters;
    private final long seed;

    public KinSelectionExperiment() {
        this(new SimulationParameters(
                400,
                4,
                80,
                1.0,
                2.4,
                0.8,
                0.02,
                0.5,
                0.25
        ), 42L);
    }

    public KinSelectionExperiment(SimulationParameters parameters, long seed) {
        this.parameters = parameters;
        this.seed = seed;
    }

    @Override
    public String id() {
        return "kin-selection";
    }

    @Override
    public String displayName() {
        return "亲缘选择实验";
    }

    @Override
    public KinSelectionReport run() {
        KinSelectionSimulation simulation = new KinSelectionSimulation(parameters, seed);
        SimulationResult result = simulation.run();
        GenerationStats firstGen = result.generations().get(0);
        GenerationStats finalGen = result.lastGeneration();
        StrategySnapshot altruistFinal = finalGen.snapshot(Strategy.ALTRUIST);
        StrategySnapshot selfishFinal = finalGen.snapshot(Strategy.SELFISH);

        OptionalInt takeoverGeneration = result.generations().stream()
                .filter(stats -> stats.share(Strategy.ALTRUIST) >= 0.9)
                .mapToInt(GenerationStats::generation)
                .min();

        double inclusiveAdvantage = altruistFinal.averageInclusiveFitness() - selfishFinal.averageInclusiveFitness();
        double directPenalty = altruistFinal.averageDirectFitness() - selfishFinal.averageDirectFitness();

        return new KinSelectionReport(
                parameters,
                result,
                firstGen,
                finalGen,
                takeoverGeneration.isPresent() ? takeoverGeneration.getAsInt() : -1,
                inclusiveAdvantage,
                directPenalty
        );
    }

    /**
     * 亲缘选择实验报告，提供公众号文章可直接使用的段落。
     */
    public static final class KinSelectionReport implements ExperimentReport {

        private final SimulationParameters parameters;
        private final SimulationResult simulationResult;
        private final GenerationStats firstGeneration;
        private final GenerationStats finalGeneration;
        private final int takeoverGeneration;
        private final double inclusiveAdvantage;
        private final double directPenalty;

        KinSelectionReport(SimulationParameters parameters,
                           SimulationResult simulationResult,
                           GenerationStats firstGeneration,
                           GenerationStats finalGeneration,
                           int takeoverGeneration,
                           double inclusiveAdvantage,
                           double directPenalty) {
            this.parameters = parameters;
            this.simulationResult = simulationResult;
            this.firstGeneration = firstGeneration;
            this.finalGeneration = finalGeneration;
            this.takeoverGeneration = takeoverGeneration;
            this.inclusiveAdvantage = inclusiveAdvantage;
            this.directPenalty = directPenalty;
        }

        @Override
        public String sectionTitle() {
            return "亲缘选择：汉密尔顿法则的数值验证";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            paragraphs.add(String.format(
                    "实验设定：将 %d 个个体划分为 %d 人一组的亲缘家庭，亲缘相关系数 r=%.2f，利他收益 B=%.1f，利他成本 C=%.1f，使得 rB=%.1f 大于 C=%.1f。",
                    parameters.populationSize(),
                    parameters.familySize(),
                    parameters.relatednessWithinFamily(),
                    parameters.benefit(),
                    parameters.cost(),
                    parameters.relatednessWithinFamily() * parameters.benefit(),
                    parameters.cost()
            ));
            String takeoverLabel = takeoverGeneration >= 0
                    ? "第 " + takeoverGeneration + " 代"
                    : "模拟范围内未超过 90%";
            paragraphs.add(String.format(
                    "结果摘要：起始利他者占比 %.1f%%，到第 %d 代提升至 %.1f%%，第一次突破 90%% 出现在 %s；利他者直接适合度相对劣势 %.2f，但包容适合度优势 %.2f。",
                    percentage(firstGeneration.share(Strategy.ALTRUIST)),
                    finalGeneration.generation(),
                    percentage(finalGeneration.share(Strategy.ALTRUIST)),
                    takeoverLabel,
                    directPenalty,
                    inclusiveAdvantage
            ));
            paragraphs.add("解读要点：");
            paragraphs.add("· 家庭结构让利他收益在亲属之间扩散，亲缘折扣后的收益依旧超过个人成本。");
            paragraphs.add("· 包容适合度被用于生殖抽样，利他策略在短期内就能占据主导。");
            paragraphs.add("· 每 10 代的日志可直接转换成折线图，用以展示利他策略的扩张轨迹。");
            return paragraphs;
        }

        @Override
        public List<ChartAttachment> charts() {
            List<GenerationStats> generations = simulationResult.generations();
            if (generations.isEmpty()) {
                return List.of();
            }

            List<Double> generationIndex = new ArrayList<>(generations.size());
            List<Double> altruistShare = new ArrayList<>(generations.size());
            List<Double> selfishShare = new ArrayList<>(generations.size());
            List<Double> altruistInclusive = new ArrayList<>(generations.size());
            List<Double> selfishInclusive = new ArrayList<>(generations.size());
            List<Double> altruistDirect = new ArrayList<>(generations.size());
            List<Double> selfishDirect = new ArrayList<>(generations.size());

            for (GenerationStats stats : generations) {
                generationIndex.add((double) stats.generation());

                StrategySnapshot altruistSnapshot = stats.snapshot(Strategy.ALTRUIST);
                StrategySnapshot selfishSnapshot = stats.snapshot(Strategy.SELFISH);

                altruistShare.add(percentage(stats.share(Strategy.ALTRUIST)));
                selfishShare.add(percentage(stats.share(Strategy.SELFISH)));

                altruistInclusive.add(altruistSnapshot.averageInclusiveFitness());
                selfishInclusive.add(selfishSnapshot.averageInclusiveFitness());

                altruistDirect.add(altruistSnapshot.averageDirectFitness());
                selfishDirect.add(selfishSnapshot.averageDirectFitness());
            }

            return List.of(
                    new ChartAttachment(
                            "strategy-share.png",
                            "利他者与自私者占比变化",
                            "代数",
                            "占比（%）",
                            List.of(
                                    ChartSeries.of("利他者占比", generationIndex, altruistShare),
                                    ChartSeries.of("自私者占比", generationIndex, selfishShare)
                            )
                    ),
                    new ChartAttachment(
                            "inclusive-fitness.png",
                            "包容适合度对比",
                            "代数",
                            "平均适合度",
                            List.of(
                                    ChartSeries.of("利他者包容适合度", generationIndex, altruistInclusive),
                                    ChartSeries.of("自私者包容适合度", generationIndex, selfishInclusive)
                            )
                    ),
                    new ChartAttachment(
                            "direct-fitness.png",
                            "直接适合度对比",
                            "代数",
                            "平均适合度",
                            List.of(
                                    ChartSeries.of("利他者直接适合度", generationIndex, altruistDirect),
                                    ChartSeries.of("自私者直接适合度", generationIndex, selfishDirect)
                            )
                    )
            );
        }

        public SimulationResult simulationResult() {
            return simulationResult;
        }

        private static double percentage(double value) {
            return 100 * value;
        }
    }
}
