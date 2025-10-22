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

            paragraphs.add("### 爆款开场：误操作频发的团队还能撑住合作吗？");
            paragraphs.add(String.format(
                    "想象一场 96 人的跨部门协作大会：语音延迟、按钮误触、消息误读——平均 %.1f%% 的动作都会被“噪声”翻转。谁能在这样的高压环境里带队冲破囚徒困境？这篇稿件给你现成答案。",
                    noisePercent
            ));

            paragraphs.add("### 为什么读者会停下滑动");
            paragraphs.add("- 现实共鸣：远程协作、开源项目、联盟营销每天都在随机匹配新的搭子。");
            paragraphs.add("- 情绪冲突：宽容者居然能打败永远合作的老好人？数据会颠覆直觉。");
            paragraphs.add("- 传播价值：结尾提供团队治理模板，适合配图扩散到管理、社科号。");

            paragraphs.add("### 实验怎么搭，才能讲得有说服力");
            paragraphs.add(String.format(
                    "- 对局：每场 %d 轮重复囚徒困境，支付矩阵 R=%.0f / T=%.0f / P=%.0f / S=%.0f，保证“犯错成本”远高于“守序收益”。",
                    settings.rounds(),
                    settings.reward(),
                    settings.temptation(),
                    settings.punishment(),
                    settings.sucker()));
            paragraphs.add(String.format(
                    "- 参赛阵容：%d 种性格（从永远合作到严厉惩罚者）各派 %d 人，总计 %d 名角色，随机重排 %d 轮，让每个人都遇到足够多的陌生队友。",
                    strategies.size(),
                    agentsPerStrategy,
                    totalAgents,
                    encounterRounds));
            paragraphs.add(String.format(
                    "- 噪声机制：%.1f%% 的动作被强制翻转，模拟误操作、会议延迟或情绪失控，逼策略设计“容错恢复”方案。",
                    noisePercent
            ));

            if (!agentRankings.isEmpty()) {
                paragraphs.add("### 数据剧情：冠军凭什么赢、谁又跌出局");
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
                    double scoreGap = champion.meanScore() - runnerUp.meanScore();
                    double coopGap = champion.meanCooperationRate() - runnerUp.meanCooperationRate();
                    paragraphs.add(String.format(
                            "- 冠军比亚军场均多拿 %.2f 分，合作率%s %.1f 个百分点——宽容版以牙还牙虽然没那么“乖”，却用迅速修复赢下长期收益。",
                            scoreGap,
                            coopGap >= 0 ? "高出" : "低于",
                            Math.abs(coopGap) * 100
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
                paragraphs.add("### 阵营风向：整队复制就能用的策略模版");
                StrategyPerformance topStrategy = strategyRankings.get(0);
                paragraphs.add(String.format(
                        "- 冠军阵营 %s：每轮平均 %.3f 分，合作率 %.1f%%，互惠率 %.1f%%，标准差 %.2f，适合作为“默认协作协议”。",
                        topStrategy.strategy().displayName(),
                        topStrategy.meanScorePerRound(settings.rounds()),
                        topStrategy.meanCooperationRate() * 100,
                        topStrategy.meanMutualCooperationRate() * 100,
                        topStrategy.scoreStdDeviation()
                ));
                StrategyPerformance bottomStrategy = strategyRankings.get(strategyRankings.size() - 1);
                paragraphs.add(String.format(
                        "- 末位阵营 %s：合作率只有 %.1f%%，说明“只惩罚不复原”的套路在噪声环境里最容易崩，适合写成失败案例。",
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

            paragraphs.add("### 将数字转成读者关心的意义");
            paragraphs.add(String.format(
                    "- 总平均合作率 %.1f%%，告诉我们“信任＋可追责＋快速修复”是随机团队的最低配置。",
                    averageCooperationRate * 100
            ));
            if (!strategyRankings.isEmpty()) {
                StrategyPerformance topStrategy = strategyRankings.get(0);
                paragraphs.add(String.format(
                        "- 宽容型冠军 %s 提供了完整的节奏：先给善意、发现背刺立刻反击、随后抛橄榄枝。",
                        topStrategy.strategy().displayName()
                ));
            }
            paragraphs.add(String.format(
                    "- 平均互惠率 %.1f%%，比单向合作更重要——适合引出团队复盘、OKR 共建或“周四检讨会”这些组织场景。",
                    averageMutualRate * 100
            ));

            paragraphs.add("### 写稿小贴士（直接复制到排版里）");
            paragraphs.add("- 开头抛“误操作率 1.5% 挤爆协作”这句冲突，再嵌入冠军 vs. 亚军得分图，读者立刻代入。");
            paragraphs.add("- 中段用垫底角色做反面人物，穿插一则真实职场翻车案例，形成情绪共鸣。");
            paragraphs.add(String.format(
                    "- 结尾用“平均互惠率 %.1f%% 说明对话机制比单向善意更重要”收束，顺带引导读者留言分享处理冲突的经验。",
                    averageMutualRate * 100
            ));
            paragraphs.add("- 想要多稿拆分？冠军策略写成正面案例、永远背叛写成避坑指南，再加一篇管理者操作手册即可。");

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
                            "Average Score per Round by Strategy Rank",
                            "Strategy Rank",
                            "Average Score per Round",
                            List.of(ChartSeries.of("Average Score per Round", strategyRankIndex, meanScorePerRound))
                    ),
                    new ChartAttachment(
                            "strategy-cooperation-rate.png",
                            "Cooperation Rate by Strategy Rank",
                            "Strategy Rank",
                            "Cooperation Rate (%)",
                            List.of(ChartSeries.of("Average Cooperation Rate", strategyRankIndex, cooperationRate))
                    ),
                    new ChartAttachment(
                            "agent-score.png",
                            "Agent Score Distribution",
                            "Agent Rank",
                            "Total Score",
                            List.of(ChartSeries.of("Total Score", agentRankIndex, agentScore))
                    )
            );
        }
    }
}
