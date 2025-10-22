package com.river.experiment.staghunt;

import java.util.List;

/**
 * 鹿猎博弈模拟的完整结果。
 */
public record StagHuntResult(List<StagHuntGeneration> generations) {

    public StagHuntGeneration firstGeneration() {
        return generations.get(0);
    }

    public StagHuntGeneration lastGeneration() {
        return generations.get(generations.size() - 1);
    }
}
