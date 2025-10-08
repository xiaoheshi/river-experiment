package com.river.experiment.cooperation;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 协同进化（重复囚徒困境）实验封装。
 */
public final class CooperationExperiment implements Experiment<CooperationExperiment.CooperationReport> {

    private final MatchSettings settings;
    private final List<CooperationStrategy> strategies;
    private final long seed;
    private final int agentsPerStrategy;
    private final int encounterRounds;

    public CooperationExperiment() {
        this(new MatchSettings(
                200,
                0.015,
                3.0,
                5.0,
                1.0,
                0.0
        ), defaultStrategies(), 2025L, 12, 30);
    }

    public CooperationExperiment(MatchSettings settings,
                                 List<CooperationStrategy> strategies,
                                 long seed,
                                 int agentsPerStrategy,
                                 int encounterRounds) {
        this.settings = settings;
        this.strategies = List.copyOf(strategies);
        this.seed = seed;
        this.agentsPerStrategy = agentsPerStrategy;
        this.encounterRounds = encounterRounds;
    }

    @Override
    public String id() {
        return "cooperation-tournament";
    }

    @Override
    public String displayName() {
        return "协同进化锦标赛";
    }

    @Override
    public CooperationReport run() {
        CooperationTournament tournament = new CooperationTournament(
                settings,
                strategies,
                seed,
                agentsPerStrategy,
                encounterRounds
        );
        TournamentResult result = tournament.run();
        return new CooperationReport(settings, strategies, result, agentsPerStrategy, encounterRounds);
    }

    private static List<CooperationStrategy> defaultStrategies() {
        return List.of(
                CooperationStrategy.ALWAYS_COOPERATE,
                CooperationStrategy.ALWAYS_DEFECT,
                CooperationStrategy.TIT_FOR_TAT,
                CooperationStrategy.GENEROUS_TIT_FOR_TAT,
                CooperationStrategy.GRIM_TRIGGER,
                CooperationStrategy.WIN_STAY_LOSE_SHIFT,
                CooperationStrategy.SUSPICIOUS_TIT_FOR_TAT,
                CooperationStrategy.RANDOM_TIT_FOR_TAT
        );
    }

    /**
     * 锦标赛报告，描述策略和角色层面的表现。
     */
    public static final class CooperationReport implements ExperimentReport {

        private final MatchSettings settings;
        private final List<CooperationStrategy> strategies;
        private final TournamentResult result;
        private final int agentsPerStrategy;
        private final int encounterRounds;

        CooperationReport(MatchSettings settings,
                          List<CooperationStrategy> strategies,
                          TournamentResult result,
                          int agentsPerStrategy,
                          int encounterRounds) {
            this.settings = settings;
            this.strategies = strategies;
            this.result = result;
            this.agentsPerStrategy = agentsPerStrategy;
            this.encounterRounds = encounterRounds;
        }

        @Override
        public String sectionTitle() {
            return "协同的进化：重复囚徒困境的锦标赛观察";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            String noiseDescription = settings.noiseProbability() == 0
                    ? "在零噪声环境中"
                    : String.format("在含 %.1f%% 噪声的环境中", settings.noiseProbability() * 100);
            paragraphs.add(String.format(
                    "实验设定：%s进行 %d 轮重复囚徒困境，对局支付矩阵为 R=%.0f、T=%.0f、P=%.0f、S=%.0f；每种策略投放 %d 名角色，随机匹配 %d 轮以确保参赛次数一致。",
                    noiseDescription,
                    settings.rounds(),
                    settings.reward(),
                    settings.temptation(),
                    settings.punishment(),
                    settings.sucker(),
                    agentsPerStrategy,
                    encounterRounds
            ));
            paragraphs.add("参赛策略与行为解读：");
            for (CooperationStrategy strategy : strategies) {
                paragraphs.add(String.format("· %s：%s", strategy.displayName(), strategy.description()));
            }

            List<AgentPerformance> agentRankings = result.agentPerformances();
            paragraphs.add(String.format("角色排行榜（共 %d 名角色，全部结果如下）：", agentRankings.size()));
            for (int i = 0; i < agentRankings.size(); i++) {
                AgentPerformance performance = agentRankings.get(i);
                paragraphs.add(formatAgentLine(i + 1, performance));
            }

            AgentPerformance champion = result.topAgent();
            AgentPerformance runnerUp = agentRankings.size() > 1 ? agentRankings.get(1) : null;
            if (champion != null && runnerUp != null) {
                paragraphs.add(String.format(
                        "冠军解析：“%s”比“%s”场均多得 %.2f 分，合作率高出 %.1f 个百分点，显示其在随机对手环境下的稳健性。",
                        champion.agentId(),
                        runnerUp.agentId(),
                        champion.meanScore() - runnerUp.meanScore(),
                        (champion.meanCooperationRate() - runnerUp.meanCooperationRate()) * 100
                ));
            }

            List<StrategyPerformance> strategyRankings = result.strategyPerformances();
            paragraphs.add("角色类型（策略）综合排名：");
            for (int i = 0; i < strategyRankings.size(); i++) {
                StrategyPerformance performance = strategyRankings.get(i);
                paragraphs.add(String.format(
                        "· 第 %d 名 %s —— 场均累计得分 %.2f（折合每轮 %.3f），平均合作率 %.1f%%、互惠率 %.1f%%，最好/最差场次得分 %.2f / %.2f，参赛场次均值 %d（得分标准差 %.2f）。",
                        i + 1,
                        performance.strategy().displayName(),
                        performance.meanScore(),
                        performance.meanScorePerRound(settings.rounds()),
                        performance.meanCooperationRate() * 100,
                        performance.meanMutualCooperationRate() * 100,
                        performance.bestMatchScore(),
                        performance.worstMatchScore(),
                        performance.matches(),
                        performance.scoreStdDeviation()
                ));
            }

            paragraphs.add("按策略分组的角色排名（括号内为综合排名）：");
            for (CooperationStrategy strategy : strategies) {
                List<AgentPerformance> members = agentRankings.stream()
                        .filter(performance -> performance.strategy() == strategy)
                        .sorted(Comparator.comparingDouble(AgentPerformance::meanScore).reversed())
                        .toList();
                paragraphs.add(String.format("【%s】", strategy.displayName()));
                for (int memberIndex = 0; memberIndex < members.size(); memberIndex++) {
                    AgentPerformance performance = members.get(memberIndex);
                    int globalRank = agentRankings.indexOf(performance) + 1;
                    paragraphs.add(String.format(
                            "%s（综合第 %d 名）",
                            formatAgentLine(memberIndex + 1, performance),
                            globalRank
                    ));
                }
            }

            double averageCooperationRate = agentRankings.stream()
                    .mapToDouble(AgentPerformance::meanCooperationRate)
                    .average()
                    .orElse(0.0);
            double averageMutualRate = agentRankings.stream()
                    .mapToDouble(AgentPerformance::meanMutualCooperationRate)
                    .average()
                    .orElse(0.0);
            paragraphs.add(String.format(
                    "整体观察：各角色平均合作率 %.1f%%，平均互惠率 %.1f%%。友善、报复与宽容的组合在随机配对中依旧能维持互惠，同时增强了长期得分的稳定性。",
                    averageCooperationRate * 100,
                    averageMutualRate * 100
            ));

            return paragraphs;
        }

        private String formatAgentLine(int rankWithinStrategy, AgentPerformance performance) {
            return String.format(
                    "· 第 %d 名 %s（%s） —— 每场累计得分 %.2f（折合每轮 %.3f），合作率 %.1f%%、背叛率 %.1f%%，互惠率 %.1f%%，最好/最差比赛得分 %.2f / %.2f，参与对局 %d 场（得分标准差 %.2f）。",
                    rankWithinStrategy,
                    performance.agentId(),
                    performance.strategy().displayName(),
                    performance.meanScore(),
                    performance.meanScorePerRound(settings.rounds()),
                    performance.meanCooperationRate() * 100,
                    performance.meanDefectionRate() * 100,
                    performance.meanMutualCooperationRate() * 100,
                    performance.bestMatchScore(),
                    performance.worstMatchScore(),
                    performance.matches(),
                    performance.scoreStdDeviation()
            );
        }
    }
}
