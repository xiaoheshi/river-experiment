package com.river.experiment.cooperation;

import java.util.List;
import java.util.Random;

/**
 * 参与锦标赛的策略集合，使用枚举方便展示与配置。
 */
public enum CooperationStrategy {

    /**
     * 永远合作：无条件合作，模拟完全信任、无防备的个体。
     */
    ALWAYS_COOPERATE("永远合作", "无条件合作，代表极端的信任型角色。") {
        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            return Action.COOPERATE;
        }
    },

    /**
     * 永远背叛：无条件背叛，代表极端自利的角色。
     */
    ALWAYS_DEFECT("永远背叛", "无条件背叛，体现完全自利与防御的角色。") {
        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            return Action.DEFECT;
        }
    },

    /**
     * 以牙还牙：先合作，然后复刻对手上一轮的动作。
     */
    TIT_FOR_TAT("以牙还牙", "先合作，再复刻对手上一轮动作的经典互惠策略。") {
        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            if (roundIndex == 0) {
                return Action.COOPERATE;
            }
            return opponentHistory.get(roundIndex - 1);
        }
    },

    /**
     * 宽容版以牙还牙：对单次背叛保持 30% 的概率宽恕。
     */
    GENEROUS_TIT_FOR_TAT("宽容版以牙还牙", "在对手背叛后以 30% 概率原谅，避免报复循环。") {
        private static final double FORGIVE_PROBABILITY = 0.3;

        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            if (roundIndex == 0) {
                return Action.COOPERATE;
            }
            Action opponentLast = opponentHistory.get(roundIndex - 1);
            if (opponentLast == Action.DEFECT && random.nextDouble() < FORGIVE_PROBABILITY) {
                return Action.COOPERATE;
            }
            return opponentLast;
        }
    },

    /**
     * 严厉惩罚者：一旦对手背叛就永远背叛。
     */
    GRIM_TRIGGER("严厉惩罚者", "初始合作，但一旦发现背叛就永久惩罚。") {
        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            boolean opponentEverDefected = opponentHistory.contains(Action.DEFECT);
            if (opponentEverDefected) {
                return Action.DEFECT;
            }
            return Action.COOPERATE;
        }
    },

    /**
     * 赢则守输则换：上一轮达成一致则复用该动作，否则切换。
     */
    WIN_STAY_LOSE_SHIFT("赢则守输则换", "上一轮赢则保持动作，未赢则切换，代表经验主义角色。") {
        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            if (roundIndex == 0) {
                return Action.COOPERATE;
            }
            Action lastSelf = selfHistory.get(roundIndex - 1);
            Action lastOpponent = opponentHistory.get(roundIndex - 1);
            boolean lastRoundWin = lastSelf == lastOpponent;
            return lastRoundWin ? lastSelf : lastSelf.opposite();
        }
    },

    /**
     * 怀疑型以牙还牙：首轮先背叛，之后复制对方行为。
     */
    SUSPICIOUS_TIT_FOR_TAT("怀疑型以牙还牙", "首轮先试探性背叛，再复制对手上一轮动作。") {
        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            if (roundIndex == 0) {
                return Action.DEFECT;
            }
            return opponentHistory.get(roundIndex - 1);
        }
    },

    /**
     * 随机触发以牙还牙：默认复制对手上一轮，间歇性随机合作。
     */
    RANDOM_TIT_FOR_TAT("随机触发以牙还牙", "复刻对手动作，但以 20% 概率主动合作，模拟冲动友善。") {
        private static final double RANDOM_COOPERATION = 0.2;

        @Override
        public Action decide(int roundIndex,
                             List<Action> selfHistory,
                             List<Action> opponentHistory,
                             Random random) {
            if (roundIndex == 0) {
                return random.nextDouble() < 0.5 ? Action.COOPERATE : Action.DEFECT;
            }
            if (random.nextDouble() < RANDOM_COOPERATION) {
                return Action.COOPERATE;
            }
            return opponentHistory.get(roundIndex - 1);
        }
    };

    private final String displayName;
    private final String description;

    CooperationStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    /**
     * 根据过往历史与随机因素选择当前动作。
     */
    public abstract Action decide(int roundIndex,
                                  List<Action> selfHistory,
                                  List<Action> opponentHistory,
                                  Random random);
}
