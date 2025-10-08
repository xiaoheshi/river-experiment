package com.river.experiment.cooperation;

/**
 * 囚徒困境中的动作选择。
 */
public enum Action {
    COOPERATE,
    DEFECT;

    /**
     * 返回相反动作，用于处理误操作噪声。
     */
    public Action opposite() {
        return this == COOPERATE ? DEFECT : COOPERATE;
    }
}
