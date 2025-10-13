# 公共物品博弈实验

引入“合作者—搭便车者—旁观者”三类策略，使用蒙特卡洛复制器模拟公共物品博弈的演化。旁观者为群体提供随时退出的保底收益，可抑制搭便车现象。

运行命令：

```bash
mvn -q "-DskipTests" "-Dfile.encoding=UTF-8" \
    -Dexec.mainClass=com.river.experiment.publicgoods.PublicGoodsApp exec:java
```

模型输出每代策略占比与平均收益，可据此绘制合作率/收益曲线，对比有无旁观者退出选项下的差异。
