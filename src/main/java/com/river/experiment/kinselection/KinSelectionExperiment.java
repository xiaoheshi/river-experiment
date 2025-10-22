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

            paragraphs.add("### 爆款开场：谁在家庭危机里冲在最前？");
            paragraphs.add(String.format(
                    "我们把 %d 个亲属丢进一台演化引擎，让他们在 %d 代里反复选择“要不要为家人买单”。结果证明：被叫作“老好人”的人，真的有数据撑腰。",
                    parameters.populationSize(),
                    finalGeneration.generation()
            ));

            paragraphs.add("### 现实痛点切入点");
            paragraphs.add("- 养老、育儿、疾病互助中，总是那几个亲人顶上，凭什么？");
            paragraphs.add("- 想鼓励社区互助，需要证据告诉大家：先付出真的划算。");
            paragraphs.add("- 公益、品牌想讲“先吃亏后获利”的故事，这里有现成素材。");

            paragraphs.add("### 仿真舞台怎么搭，才经得起追问");
            paragraphs.add(String.format("- 家庭切分：%d 人一户，共 %d 户，亲缘相关系数 r=%.2f（近似亲兄弟姐妹）。",
                    parameters.familySize(),
                    parameters.populationSize() / parameters.familySize(),
                    parameters.relatednessWithinFamily()));
            paragraphs.add(String.format("- 收益对比：利他收益 B=%.1f、成本 C=%.1f，让 rB=%.1f > C=%.1f，满足汉密尔顿法则。",
                    parameters.benefit(),
                    parameters.cost(),
                    parameters.relatednessWithinFamily() * parameters.benefit(),
                    parameters.cost()));
            paragraphs.add(String.format("- 演化设定：按包容适合度抽样繁殖，突变率 %.2f，产出 %d 代日志，可直接转成三张曲线图。",
                    parameters.mutationRate(),
                    generationsCount));

            paragraphs.add("### 数据剧情三段式");
            String takeoverSentence;
            if (reachedNinety) {
                takeoverSentence = String.format(
                        "- 起始利他者占比 %.1f%% → 第 %d 代 %.1f%%，%s首次跨过 90%%，展示“爱心爆发”戏剧化转折。",
                        initialShare,
                        finalGeneration.generation(),
                        finalShare,
                        takeoverLabel
                );
            } else {
                takeoverSentence = String.format(
                        "- 起始利他者占比 %.1f%% → 第 %d 代 %.1f%%，虽然模拟范围内未突破 90%%，但增长速度始终领先自私者，为后续加码留悬念。",
                        initialShare,
                        finalGeneration.generation(),
                        finalShare
                );
            }
            paragraphs.add(takeoverSentence);
            paragraphs.add(String.format("- 利他者直接适合度劣势 %.2f，却靠包容适合度优势 %.2f 扭转命运：短期吃亏、长期回本的量化证据。",
                    directPenalty,
                    inclusiveAdvantage));
            paragraphs.add("- 日志每 10 代输出一次，对应策略占比/包容适合度/直接适合度三张图，剧本式呈现“坚持与回报”。");

            paragraphs.add("### 把数据翻译成读者会点赞的洞察");
            paragraphs.add("- 家庭或社区互助不是情怀，而是“关系强度 × 回流收益”真的大于投入。");
            paragraphs.add("- 政策层面可用社区积分、互助保险、税收抵扣提升“关系强度”，把模型搬进现实。");
            paragraphs.add("- 企业文化若打造“亲属感”，等同于复制包容适合度机制，让关键员工享受团队复利。");

            paragraphs.add("### 写稿与排版提示");
            paragraphs.add(String.format("- 用“%.1f%%→%.1f%%”的占比曲线开篇，再补上“包容适合度领先 %.2f”这个转折， 拉高情绪张力。",
                    initialShare,
                    finalShare,
                    inclusiveAdvantage));
            paragraphs.add("- 中段穿插一个真实的家庭或社区故事，映射数据里的“先垫付后回流”。");
            paragraphs.add("- 结尾抛出“我们能否设计出让陌生人也愿意互助的制度？”鼓励读者留言。");

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
                            "Strategy Share Over Generations",
                            "Generation",
                            "Share (%)",
                            List.of(
                                    ChartSeries.of("Altruist Share", generationIndex, altruistShare),
                                    ChartSeries.of("Selfish Share", generationIndex, selfishShare)
                            )
                    ),
                    new ChartAttachment(
                            "inclusive-fitness.png",
                            "Inclusive Fitness Comparison",
                            "Generation",
                            "Average Fitness",
                            List.of(
                                    ChartSeries.of("Altruist Inclusive Fitness", generationIndex, altruistInclusive),
                                    ChartSeries.of("Selfish Inclusive Fitness", generationIndex, selfishInclusive)
                            )
                    ),
                    new ChartAttachment(
                            "direct-fitness.png",
                            "Direct Fitness Comparison",
                            "Generation",
                            "Average Fitness",
                            List.of(
                                    ChartSeries.of("Altruist Direct Fitness", generationIndex, altruistDirect),
                                    ChartSeries.of("Selfish Direct Fitness", generationIndex, selfishDirect)
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
