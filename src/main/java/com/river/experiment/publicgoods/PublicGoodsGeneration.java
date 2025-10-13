package com.river.experiment.publicgoods;

/**
 * 每代公共物品博弈的占比与收益数据。
 */
public record PublicGoodsGeneration(
        int generation,
        double cooperatorShare,
        double defectorShare,
        double lonerShare,
        double cooperatorPayoff,
        double defectorPayoff,
        double lonerPayoff,
        double populationPayoff
) {
}
