# ADR-0008：Release R8 与 CI 门禁

## 背景

v2 功能完整后需可发布构建，且 PR 合入 `main` 应有自动化验证。

## 决策

- Release 开启 **R8 + shrinkResources**；ProGuard 规则维护于 `app/proguard-rules.pro`。
- 签名通过 **gitignore 的 `keystore.properties`** + 本地 `keystore/release.jks`；仓库仅提交 `keystore.properties.example`。
- **CI**（GitHub Actions）在 PR/push 时跑 `./gradlew test assembleDebug`；不在 CI 构建 signed release。
- v2 版本 **2.0.0**（versionCode 2）。

## 后果

- 贡献者本地 Release 需自行生成 keystore。
- R8 回归问题通过 release smoke 与 keep 规则迭代解决。
