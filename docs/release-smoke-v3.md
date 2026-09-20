# v3 Release 手工 Smoke 清单

在 v2.6「冷启动 → 首页 → 详情（真网 FakeStore）」基础上，v3 合入 `main` 前请用 **Release APK** 逐项自检。CI 不跑本清单；PR 描述中勾选完成项。

构建 Release APK：

```bash
# 需 keystore.properties + local.properties（Sign-In 测真 Google 账号时填写 google.web_client_id）
./gradlew assembleRelease
```

| # | 步骤 | 通过 |
|---|------|------|
| 1 | Release APK 冷启动，进入首页 | ☐ |
| 2 | 浏览 FakeStore **商品** → 详情 → 分享 deep link → 备忘录/浏览器点击链回 App | ☐ |
| 3 | **联系卖家** → 发消息 → 收到 Mock 卖家回复 | ☐ |
| 4 | Google 登录（需 `local.properties` + Cloud Console SHA-1）→ Profile 显示头像与 displayName | ☐ |
| 5 | 创建带**面交地点**的**挂牌** → 点击地图 Intent 可打开系统地图 | ☐ |
| 6 | Mock 下单 → 等待发货通知 → 点击通知 → 进入**订单详情** deep link | ☐ |
| 7 | 登出 → 仅见 guest 归属数据（订单/挂牌/会话） | ☐ |

说明：

- 步骤 4 若仅因 SHA-1 未配置导致 Sign-In 失败，不算 Release 构建失败。
- CI `release-r8` job 使用占位 `google.web_client_id`，仅验证 R8/ProGuard 编译通过。

规格：[v3.5 Release 与 CI (#34)](https://github.com/Zoti321/c2c-market/issues/34)
