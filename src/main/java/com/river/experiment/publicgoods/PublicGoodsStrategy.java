package com.river.experiment.publicgoods;

/**
 * 公共物品博弈中的策略类型。
 */
public enum PublicGoodsStrategy {
    COOPERATOR("无条件合作者", "每次参与集体行动并投入成本。"),
    DEFECTOR("搭便车者", "参与分配收益但拒绝出资。"),
    LONER("旁观者", "拒绝参与公共物品博弈，领取保底收益。");

    private final String displayName;
    private final String description;

    PublicGoodsStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }
}
