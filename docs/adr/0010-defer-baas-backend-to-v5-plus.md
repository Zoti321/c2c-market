# v4 仍 local-first，BaaS 后端延至 v5+

v4 目标是卖家闭环与订单状态机，不分散到 infra 集成。Firebase / Supabase 等 BaaS 在 ADR-0006 中已为聊天同步预留，但 v3/v4 均不引入远端依赖。v4 implement 时 Repository 保持 **Room SSOT**，并预留 `RemoteDataSource` 接口（v4 可为 NoOp 或 absent），以便 v5+ 接入 Firestore 或 Supabase 时不重写 UI。

**Status**: accepted (v4 scope)

**Considered options**: v4 即接 Firebase Firestore（拒绝，范围膨胀）、v4 即接 Supabase（拒绝，同上）、v4 local-first + v5 选型（选用，见 [`docs/research/baas-backend-options.md`](../research/baas-backend-options.md)）

**v4 约束**：

- 不添加 `com.google.firebase` 或 Supabase SDK 依赖
- 聊天、挂牌、订单状态变更以 Room 为唯一真相源
- Google Sign-In 继续 Credential Manager + DataStore（不切换 Firebase Auth）

**v5+ 触发条件**（任一满足再开 v5 Map）：

- 需要跨设备同步聊天或挂牌
- 需要 FCM 远端 Push
- 需要挂牌图片云端存储与分享
