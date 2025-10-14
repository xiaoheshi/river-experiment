package com.river.experiment.cooperation;

import com.river.experiment.core.Experiment;
import com.river.experiment.core.ExperimentReport;
import com.river.experiment.core.chart.ChartAttachment;
import com.river.experiment.core.chart.ChartSeries;

import java.util.ArrayList;
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

        public MatchSettings settings() {
            return settings;
        }

        public TournamentResult tournamentResult() {
            return result;
        }

        @Override
        public String sectionTitle() {
            return "噪声派对锦标赛：宽容策略如何赢下 96 人囚徒困境";
        }

        @Override
        public List<String> paragraphs() {
            List<String> paragraphs = new ArrayList<>();
            double noisePercent = settings.noiseProbability() * 100;
            int totalAgents = strategies.size() * agentsPerStrategy;
            List<AgentPerformance> agentRankings = result.agentPerformances();
            List<StrategyPerformance> strategyRankings = result.strategyPerformances();

            paragraphs.add(String.format(
                    "96 位策略选手在含 %.1f%% 噪声的随机派对里混战，我们想看谁能在误操作频发的现实世界守住合作。",
                    noisePercent
            ));

            paragraphs.add("### 随机派对的底层规则");
            paragraphs.add(String.format("- 重复囚徒困境每场进行 %d 轮，支付矩阵 R=%.0f / T=%.0f / P=%.0f / S=%.0f。",
                    settings.rounds(),
                    settings.reward(),
                    settings.temptation(),
                    settings.punishment(),
                    settings.sucker()));
            paragraphs.add(String.format("- %d 种性格（永远合作、宽容/怀疑版以牙还牙、严厉惩罚者等）各派 %d 人，总计 %d 名角色，随机重排 %d 轮。",
                    strategies.size(),
                    agentsPerStrategy,
                    totalAgents,
                    encounterRounds));
            paragraphs.add(String.format(
                    "- 噪声 %.1f%% 会把动作翻转，逼着参赛者设计容错和恢复流程。",
                    noisePercent
            ));

            if (!agentRankings.isEmpty()) {
                paragraphs.add("### 冠军榜：谁能在噪声里稳住合作");
                int topAgentLimit = Math.min(3, agentRankings.size());
                for (int i = 0; i < topAgentLimit; i++) {
                    AgentPerformance performance = agentRankings.get(i);
                    paragraphs.add(String.format(
                            "- TOP %d %s（%s）：场均每轮 %.3f 分，合作率 %.1f%%，互惠率 %.1f%%，最好/最差场次 %.2f / %.2f。",
                            i + 1,
                            performance.agentId(),
                            performance.strategy().displayName(),
                            performance.meanScorePerRound(settings.rounds()),
                            performance.meanCooperationRate() * 100,
                            performance.meanMutualCooperationRate() * 100,
                            performance.bestMatchScore(),
                            performance.worstMatchScore()
                    ));
                }
                if (agentRankings.size() >= 2) {
                    AgentPerformance champion = agentRankings.get(0);
                    AgentPerformance runnerUp = agentRankings.get(1);
                    paragraphs.add(String.format(
                            "- 冠军比亚军场均多拿 %.2f 分、合作率高出 %.1f 个百分点，是“先友善→立刻惩罚→快速复原”的最佳注脚。",
                            champion.meanScore() - runnerUp.meanScore(),
                            (champion.meanCooperationRate() - runnerUp.meanCooperationRate()) * 100
                    ));
                }
                AgentPerformance underdog = agentRankings.get(agentRankings.size() - 1);
                paragraphs.add(String.format(
                        "- 垫底提醒：%s（%s）场均只拿 %.2f 分，合作率 %.1f%%，几乎成了他人警示录。",
                        underdog.agentId(),
                        underdog.strategy().displayName(),
                        underdog.meanScore(),
                        underdog.meanCooperationRate() * 100
                ));
            }

            if (!strategyRankings.isEmpty()) {
                paragraphs.add("### 策略阵营风向");
                StrategyPerformance topStrategy = strategyRankings.get(0);
                paragraphs.add(String.format(
                        "- 冠军阵营 %s：每轮平均 %.3f 分，合作率 %.1f%%，互惠率 %.1f%%，标准差 %.2f。",
                        topStrategy.strategy().displayName(),
                        topStrategy.meanScorePerRound(settings.rounds()),
                        topStrategy.meanCooperationRate() * 100,
                        topStrategy.meanMutualCooperationRate() * 100,
                        topStrategy.scoreStdDeviation()
                ));
                StrategyPerformance bottomStrategy = strategyRankings.get(strategyRankings.size() - 1);
                paragraphs.add(String.format(
                        "- 末位阵营 %s：合作率只有 %.1f%%，说明“只惩罚不复原”的套路在噪声环境里最容易崩。",
                        bottomStrategy.strategy().displayName(),
                        bottomStrategy.meanCooperationRate() * 100
                ));
            }

            double averageCooperationRate = agentRankings.stream()
                    .mapToDouble(AgentPerformance::meanCooperationRate)
                    .average()
                    .orElse(0.0);
            double averageMutualRate = agentRankings.stream()
                    .mapToDouble(AgentPerformance::meanMutualCooperationRate)
                    .average()
                    .orElse(0.0);

            paragraphs.add("### 三个现实启示");
            paragraphs.add(String.format(
                    "- 总平均合作率 %.1f%%，说明只要允许快速修复，合作就能在随机搭子里坐稳半壁江山。",
                    averageCooperationRate * 100
            ));
            if (!strategyRankings.isEmpty()) {
                StrategyPerformance topStrategy = strategyRankings.get(0);
                paragraphs.add(String.format(
                        "- 宽容型冠军 %s 把“犯错后给台阶”写进流程，是团队制度里最值得抄的玩法。",
                        topStrategy.strategy().displayName()
                ));
            }
            paragraphs.add(String.format(
                    "- 平均互惠率 %.1f%%，比单纯的合作率更能打，告诉我们“对话机制”比“单向善意”重要得多。",
                    averageMutualRate * 100
            ));

            paragraphs.add("### 写稿小贴士");
            paragraphs.add("- 用“冠军比亚军多拿多少分”开场，再贴前 3 名的柱状图，读者会立刻代入“职场策略赛”。");
            paragraphs.add("- 把 1.5% 噪声翻译成“误操作率”，辅以真实案例，能自然过渡到团队容错话题。");
            paragraphs.add(String.format(
                    "- 结尾抛出“平均互惠率 %.1f%%”这句金句，就能把伦理讨论拉回机制设计。",
                    averageMutualRate * 100
            ));

            return paragraphs;
        }

        @Override
        public List<ChartAttachment> charts() {
            List<StrategyPerformance> strategyRankings = result.strategyPerformances();
            if (strategyRankings.isEmpty()) {
                return List.of();
            }

            List<Double> strategyRankIndex = new ArrayList<>(strategyRankings.size());
            List<Double> meanScorePerRound = new ArrayList<>(strategyRankings.size());
            List<Double> cooperationRate = new ArrayList<>(strategyRankings.size());
            for (int i = 0; i < strategyRankings.size(); i++) {
                StrategyPerformance performance = strategyRankings.get(i);
                strategyRankIndex.add((double) (i + 1));
                meanScorePerRound.add(performance.meanScorePerRound(settings.rounds()));
                cooperationRate.add(performance.meanCooperationRate() * 100);
            }

            List<AgentPerformance> agents = result.agentPerformances();
            List<Double> agentRankIndex = new ArrayList<>(agents.size());
            List<Double> agentScore = new ArrayList<>(agents.size());
            for (int i = 0; i < agents.size(); i++) {
                agentRankIndex.add((double) (i + 1));
                agentScore.add(agents.get(i).meanScore());
            }

            return List.of(
                    new ChartAttachment(
                            "strategy-score-per-round.png",
                            "策略排名与场均得分",
                            "策略排名",
                            "场均得分",
                            List.of(ChartSeries.of("场均得分", strategyRankIndex, meanScorePerRound))
                    ),
                    new ChartAttachment(
                            "strategy-cooperation-rate.png",
                            "策略合作率走势",
                            "策略排名",
                            "合作率（%）",
                            List.of(ChartSeries.of("平均合作率", strategyRankIndex, cooperationRate))
                    ),
                    new ChartAttachment(
                            "agent-score.png",
                            "角色得分分布",
                            "角色排名",
                            "累计得分",
                            List.of(ChartSeries.of("累计得分", agentRankIndex, agentScore))
                    )
            );
        }
    }
}
