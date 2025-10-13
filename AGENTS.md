# Repository Guidelines

本项目使用 Java 实现多个经典演化实验，并产出适合发布于微信公众号的 Markdown 文章与配套图表，便于快速复述实验结论。

## 项目结构与模块组织
- `pom.xml` 声明全部依赖与插件，`com.river.experiment.core` 保留共用的实验接口 `Experiment` 与报告抽象 `ExperimentReport`。
- 各理论模块位于 `src/main/java/com/river/experiment/<domain>`，例如 `kinselection`、`cooperation`、`hawkdove`、`publicgoods`，分别提供入口类 `KinSelectionApp`、`CooperationApp`、`HawkDoveApp`、`PublicGoodsApp` 及模块内 `README.md` 用于记录假设与参数。
- 文章草稿与实验说明存于 `articles/`，若调整仿真逻辑或输出，需要同步更新对应解读。
- 自动导出的文章保存在 `articles/generated/<experiment-id>.md`，配图位于 `articles/generated/assets/<experiment-id>/`，可直接嵌入对应的微信公众号文章。

## 构建、测试与开发命令
- `mvn -q -DskipTests package`：快速编译打包全部模块，提交前确保通过。
- `mvn -q "-DskipTests" "-Dfile.encoding=UTF-8" -Dexec.mainClass=com.river.experiment.kinselection.KinSelectionApp exec:java`：运行亲缘选择实验；将主类替换为 `com.river.experiment.cooperation.CooperationApp` 可启动协同行为锦标赛。
- macOS/Linux 使用 `\` 换行，Windows PowerShell 使用 `^`；若控制台出现中文乱码，先执行 `chcp 65001` 切换至 UTF-8。

## 编码风格与命名规范
- 采用 Java 17 风格：四空格缩进、花括号与声明同行，类名与策略名使用 `PascalCase`，方法与字段使用 `camelCase`，常量保持全大写下划线。
- 包名全部小写，核心抽象归档在 `core`；新增实验需实现 `Experiment` 接口并返回对应 `ExperimentReport`，保证可复用性。
- 统计对象建议使用不可变设计（`record` 或终态字段），对复杂演化步骤补充简洁 Javadoc。

## 测试指南
- 在 `src/test/java` 中镜像源代码包路径创建测试类，覆盖收益计算、世代演化与报告统计；命名采用 `*Test`。
- 默认使用 JUnit 5 执行 `mvn test`，并为新增策略编写场景级回归测试；随机过程请固定 `Random` 种子以保证重现性。
- 若引入长时间仿真，请将关键统计写入断言或快照文件，避免误回归。

## 提交与合并请求规范
- 延续当前简洁的祈使句提交信息，如 `Add kin selection payoff stats`，摘要控制在 72 字符以内，必要时在正文补充实验背景。
- PR 中需说明修改动机、主要变更、复现命令及参数，并附关键输出（日志片段或图表）；若更新了 `articles/*.md`，请在描述中说明。
- 合并前确认 `mvn -q -DskipTests package` 与相关仿真命令运行通过，确保评审者可快速复现。
