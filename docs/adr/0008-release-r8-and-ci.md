# ADR-0008：Release R8 与 CI 门禁

## 背景

v2 功能完整后需可发布构建，且 PR 合入 `main` 应有自动化验证。

## 决策

- Release 开启 **R8 + shrinkResources**；ProGuard 规则维护于 `app/proguard-rules.pro`。
- 签名通过 **gitignore 的 `keystore.properties`** + 本地 `keystore/release.jks`；仓库仅提交 `keystore.properties.example`。
- **CI**（GitHub Actions）在 PR/push 时跑四个并行 Job：
  1. **Lint + Detekt** — Android Lint（`lintDebug`）+ Kotlin 静态分析（`detekt`）
  2. **单测 + 覆盖率** — `./gradlew test assembleDebug jacocoDebugUnitTestCoverageVerification`（行覆盖率门禁 ≥15%，v4.6 起；v3.5 为 12%）
  3. **仪器测试** — 模拟器上 `connectedDebugAndroidTest`
  4. **Release R8** — CI 生成临时 keystore 后 `assembleRelease`，验证 R8/ProGuard 规则
- v2 版本 **2.0.0**（versionCode 2）；v3 合入后为 **3.0.0**（versionCode 3）；v4 合入后为 **4.0.0**（versionCode 4，Room v10）。
- v4.6 Release smoke 清单见 [`docs/release-smoke-v4.md`](../release-smoke-v4.md)。

## 后果

- 贡献者本地 Release 需自行生成 keystore；CI 使用一次性临时签名，产物不可用于上架。
- R8 回归问题由 CI release job 与 keep 规则迭代解决。
- 仪器测试依赖 GitHub Actions 模拟器，PR 合并前无需再手工跑 connected 测试（仍可在本地调试）。
