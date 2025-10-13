package com.river.experiment.publicgoods;

import java.util.List;

/**
 * 公共物品博弈的完整模拟结果。
 */
public record PublicGoodsResult(List<PublicGoodsGeneration> generations) {

    public PublicGoodsGeneration firstGeneration() {
        return generations.get(0);
    }

    public PublicGoodsGeneration lastGeneration() {
        return generations.get(generations.size() - 1);
    }
}
