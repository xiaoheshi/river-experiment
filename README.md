# 演化理论实验合集

本仓库以 Java 实现多种演化理论的数值实验，每个包代表一个独立模块，可单独运行并产出中文说明，方便撰写公众号文章或教学资料。

- `com.river.experiment.kinselection`：亲缘选择实验，验证汉密尔顿法则下利他策略的演化优势。
- `com.river.experiment.cooperation`：协同进化实验，引入多种合作/背叛策略、随机配对角色和细粒度统计。
- `com.river.experiment.core`：统一的实验接口与报告抽象，方便后续扩展更多理论。

## 构建

```bash
mvn -q -DskipTests package
```

## 分别运行实验

亲缘选择实验（PowerShell 写法；在类 Unix 终端请把 `^` 改成 `\`）：

```bash
mvn -q "-DskipTests" "-Dfile.encoding=UTF-8" ^
    -Dexec.mainClass=com.river.experiment.kinselection.KinSelectionApp exec:java
```

协同进化实验：

```bash
mvn -q "-DskipTests" "-Dfile.encoding=UTF-8" ^
    -Dexec.mainClass=com.river.experiment.cooperation.CooperationApp exec:java
```

若终端出现中文乱码，可先执行 `chcp 65001` 切换到 UTF-8。

## 协同进化模块亮点

- 新增策略：在“永远合作/背叛、以牙还牙、宽容版以牙还牙、严厉惩罚者”基础上，引入“赢则守输则换”“怀疑型以牙还牙”“随机触发以牙还牙”等角色，覆盖更多现实行为模式。
- 随机匹配：每种策略投放相同数量的角色，在多轮洗牌后两两对战，既保留随机性又保证参赛次数一致。
- 统计升级：输出包含每场累计得分、折算到“每轮得分”、合作率/背叛率、互惠率、最好/最差成绩与标准差，便于深入分析策略稳定性。

## 扩展建议

- 新增实验时创建独立包，并实现 `Experiment` 与 `ExperimentReport` 接口保持输出格式一致。
- 将日志导出为 CSV，结合 Python/R 绘制折线图、箱线图或热力图，呈现更丰富的可视化效果。
- 考虑将关键参数抽象为配置文件或命令行选项，方便批量扫描不同假设下的理论边界。
