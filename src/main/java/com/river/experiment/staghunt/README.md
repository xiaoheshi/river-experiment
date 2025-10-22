## 鹿猎信号协调实验

- **故事设定**：团队在鹿猎博弈中分为三类 — 先行喊话的「信号者」、只在看到信号后才敢押注鹿猎的「跟随者」、以及始终猎兔的「保守者」。
- **机制**：信号者每次发出开局指令需支付固定成本，但一旦有人响应就能把团队推进高收益鹿猎。我们用复制器动力学演化这三类策略的占比。
- **运行方式**：

```bash
mvn -q "-DskipTests" "-Dfile.encoding=UTF-8" \
    -Dexec.mainClass=com.river.experiment.staghunt.StagHuntApp exec:java
```

- **输出**：控制台打印关键代数的策略份额、收益与信号触发率；自动生成公众号文章草稿 `articles/generated/stag-hunt-signal.md` 以及三张折线图，位于 `articles/generated/assets/stag-hunt-signal/`。
