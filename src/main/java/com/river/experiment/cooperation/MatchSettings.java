package com.river.experiment.cooperation;

/**
 * 囚徒困境对局设置，方便调整轮数、噪声与支付矩阵。
 */
public final class MatchSettings {

    private final int rounds;
    private final double noiseProbability;
    // R：双方都合作时的奖励分（Reward for mutual cooperation）
    private final double reward;
    // T：单方背叛时背叛者获得的诱惑分（Temptation to defect）
    private final double temptation;
    // P：双方都背叛时的惩罚分（Punishment for mutual defection）
    private final double punishment;
    // S：单方合作但被对方背叛时的受害者得分（Sucker's payoff）
    private final double sucker;

    public MatchSettings(int rounds,
                         double noiseProbability,
                         double reward,
                         double temptation,
                         double punishment,
                         double sucker) {
        if (rounds <= 0) {
            throw new IllegalArgumentException("对局轮数必须大于 0。");
        }
        if (noiseProbability < 0 || noiseProbability > 1) {
            throw new IllegalArgumentException("噪声概率需介于 0 与 1 之间。");
        }
        this.rounds = rounds;
        this.noiseProbability = noiseProbability;
        this.reward = reward;
        this.temptation = temptation;
        this.punishment = punishment;
        this.sucker = sucker;
    }

    public int rounds() {
        return rounds;
    }

    public double noiseProbability() {
        return noiseProbability;
    }

    public double reward() {
        return reward;
    }

    public double temptation() {
        return temptation;
    }

    public double punishment() {
        return punishment;
    }

    public double sucker() {
        return sucker;
    }
}
