# ADR-0009：Compose UI 冒烟 Robolectric 化与 CI instrumented 占位

## 背景

[ADR-0008](0008-release-r8-and-ci.md) 规定 PR 在模拟器上跑 `connectedDebugAndroidTest`。v2.6 仅有 3 个 Compose Screen 冒烟测试（断言标题/空态/游客模式），CI 仪器测试 job 耗时 ~30–35 min（模拟器冷启动 + Gradle 冷编译），且测试本身不需要真机环境。项目已在 Room、WorkManager 等层使用 Robolectric。

## 决策

- **Compose UI 冒烟测试**从 `androidTest` 迁移到 `test`（Robolectric + `createComposeRule`），通过 Composable 默认参数注入 fake ViewModel，不使用 `@HiltAndroidTest`。
- **删除** `androidTest` 源码集及相关 Gradle 配置（`HiltTestRunner`、`enableAndroidTestCoverage` 等）。
- **CI `instrumented-tests` job** 改为 stub 占位（echo 说明，不启动模拟器），保留 workflow 槽位供将来 E2E 仪器测试启用。
- **有效 PR 门禁**仍为 4 个 job 名称不变，但实质验证为 lint + unit（含 Robolectric 冒烟 + JaCoCo）+ release；instrumented job ≤ 1 min。
- **Gradle 缓存**：lint / unit / release 三 job 使用 `gradle/actions/setup-gradle@v4`（见 [#28](https://github.com/Zoti321/c2c-market/issues/28)）。

## 后果

- PR 反馈时间显著缩短（去掉 ~30 min 模拟器 job）。
- 贡献者本地 UI 冒烟验证：`./gradlew test`，无需 `connectedDebugAndroidTest`。
- 将来若需 Navigation E2E 或多屏仪器测试，在 stub job 中恢复 `android-emulator-runner` 即可。
- ADR-0008 四 job 描述保留历史；本 ADR 记录后续调整。
