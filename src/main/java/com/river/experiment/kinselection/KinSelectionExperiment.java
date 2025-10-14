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
            return "亲缘选择：1000 代演化揭开家庭互助的底层逻辑";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            double initialShare = percentage(firstGeneration.share(Strategy.ALTRUIST));
            double finalShare = percentage(finalGeneration.share(Strategy.ALTRUIST));
            int generationsCount = simulationResult.generations().size();
            boolean reachedNinety = takeoverGeneration >= 0;
            String takeoverLabel = reachedNinety
                    ? "第 " + takeoverGeneration + " 代"
                    : "模拟范围内尚未";

            paragraphs.add(String.format(
                    "为什么每次家庭群聊出事，永远是长辈冲在最前？我们把 %d 个个体放进 %d 代的演化实验里，让数据告诉你“亲缘互助”背后的收益账。",
                    parameters.populationSize(),
                    finalGeneration.generation()
            ));

            paragraphs.add("### 仿真怎么搭");
            paragraphs.add(String.format("- 家庭结构：%d 人一户，共 %d 户，亲缘相关系数 r=%.2f。",
                    parameters.familySize(),
                    parameters.populationSize() / parameters.familySize(),
                    parameters.relatednessWithinFamily()));
            paragraphs.add(String.format("- 利他收益与成本：B=%.1f、C=%.1f，让 rB=%.1f > C=%.1f。",
                    parameters.benefit(),
                    parameters.cost(),
                    parameters.relatednessWithinFamily() * parameters.benefit(),
                    parameters.cost()));
            paragraphs.add(String.format("- 繁殖规则：按包容适合度抽样，突变率 %.2f，累计记录 %d 代演化日志。",
                    parameters.mutationRate(),
                    generationsCount));

            paragraphs.add("### 三个关键数据瞬间");
            String takeoverSentence;
            if (reachedNinety) {
                takeoverSentence = String.format(
                        "- 起始利他者占比 %.1f%% → 第 %d 代 %.1f%%，%s首次跨过 90%% 门槛。",
                        initialShare,
                        finalGeneration.generation(),
                        finalShare,
                        takeoverLabel
                );
            } else {
                takeoverSentence = String.format(
                        "- 起始利他者占比 %.1f%% → 第 %d 代 %.1f%%，虽然模拟范围内未突破 90%%，但增长速度始终领先自私者。",
                        initialShare,
                        finalGeneration.generation(),
                        finalShare
                );
            }
            paragraphs.add(takeoverSentence);
            paragraphs.add(String.format("- 利他者直接适合度劣势 %.2f，但包容适合度优势 %.2f，让“吃亏”转化为生存优势。",
                    directPenalty,
                    inclusiveAdvantage));
            paragraphs.add("- 每 10 代输出一次日志，对应三张折线图：策略占比、包容适合度、直接适合度，一目了然。");

            paragraphs.add("### 可以抛给读者的现实启示");
            paragraphs.add("- 家庭或社区互助不是情怀，而是“关系强度 × 回流收益”真的大于投入。");
            paragraphs.add("- 想在政策里激活互助，积分、信誉、税收抵扣都能放大“r”。");
            paragraphs.add("- 企业文化里营造“亲属感”，本质是在复制让贡献者拿到团队复利。");

            paragraphs.add("### 写稿时的素材提示");
            paragraphs.add(String.format("- 用“%.1f%%→%.1f%%”的占比曲线开篇，再补上“包容适合度领先 %.2f”的数据，故事张力瞬间拉满。",
                    initialShare,
                    finalShare,
                    inclusiveAdvantage));
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
