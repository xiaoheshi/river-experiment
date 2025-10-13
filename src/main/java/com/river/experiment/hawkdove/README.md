# 鹰鸽冲突实验

经典的资源争夺模型：鹰派（激进争夺）与鸽派（示弱退让）在有限资源 V 下竞争，冲突成本 C 高于收益时，纯鹰派并非演化稳定策略。实验使用复制器动力学模拟群体占比的演化。

运行命令：

```bash
mvn -q "-DskipTests" "-Dfile.encoding=UTF-8" \
    -Dexec.mainClass=com.river.experiment.hawkdove.HawkDoveApp exec:java
```

输出包含各代鹰派/鸽派占比、单次冲突的收益差、以及收敛到混合均衡的代数，可直接转换为折线图辅助教学。

运行后将在 `articles/generated/hawk-dove.md` 生成讲稿，并在 `articles/generated/assets/hawk-dove/` 输出占比与收益曲线图。
