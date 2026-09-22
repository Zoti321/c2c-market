# Firebase vs Supabase — v5 后端选型调研

> **状态**：调研完成 · 建议 v5 采用 Firebase  
> **日期**：2026-09-21  
> **关联**：[`baas-backend-options.md`](baas-backend-options.md) · [ADR-0006](../adr/0006-chat-local-first-v3.md) · [ADR-0010](../adr/0010-defer-baas-backend-to-v5-plus.md) · 路线图 [#49](https://github.com/Zoti321/c2c-market/issues/49) · 决策票 [#50](https://github.com/Zoti321/c2c-market/issues/50)

## 摘要

C2C Market v4 已完成本地卖家闭环（Room SSOT、Credential Manager 登录、`google:{sub}` 用户标识）。v5 需引入远端同步、FCM Push、挂牌图片云存储。对照官方文档与项目约束后，**推荐 Firebase（Firestore + Storage + FCM）作为 v5 后端**；Supabase 在关系模型与开源方面更有吸引力，但对本项目的 Android 集成成本、Push 与现有 Auth 架构冲突更高。

---

## 项目约束（v5 必须满足）

| 约束 | 来源 | 对选型的影响 |
|------|------|--------------|
| Room 仍为本地 SSOT | [ADR-0002](../adr/0002-room-as-local-ssot.md)、[ADR-0010](../adr/0010-defer-baas-backend-to-v5-plus.md) | 无论选哪家，Repository 需 `local + remote` 双源 sync |
| Google 登录经 Credential Manager | v3.2、`AuthRepositoryImpl` | 远端 Security Rules / RLS 须能绑定稳定用户标识 |
| userId 格式 `google:{sub}` | `CONTEXT.md`、`UserIds` | 远端主键 / 文档字段应与此一致或可映射 |
| v5 域：聊天、挂牌、订单、图片 | [baas-backend-options.md](baas-backend-options.md) | 需实时监听 + 文件上传 + Push |
| 学习项目、单人维护 | README | 优先官方 Android 示例、低运维、免费额度够用 |

---

## 维度对比

### 1. 与现有 Google 登录的集成

**现状**：`AuthRepositoryImpl` 使用 [Credential Manager](https://developer.android.com/identity/sign-in/credential-manager) + DataStore，**未**引入 Firebase Auth 或 Supabase Auth；登录后 `userId = "google:{sub}"`。

#### Firebase

[Firebase 官方文档](https://firebase.google.com/docs/auth/android/google-signin) 明确支持 **Credential Manager + Firebase Auth** 同一流程：获取 Google ID Token 后调用 `GoogleAuthProvider.getCredential(idToken, null)` → `signInWithCredential`。这与 v3.2 实现高度兼容，改动集中在 `AuthRepositoryImpl` 增加一层 Firebase 会话，**不必放弃 Credential Manager UI**。

[Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started) 依赖 `request.auth`（须 Firebase Authentication）。因此 v5 接入 Firestore 时，**需增加 Firebase Auth 作为安全层**，但可继续用 `google:{sub}` 作为业务 userId（Firestore 文档字段），`request.auth.uid` 或 custom claims 做规则校验。

[Custom Authentication](https://firebase.google.com/docs/auth/android/custom-auth) 可免 Google 直连 Firebase Auth，但需自建服务端签发 custom token——对学习项目过重，**不推荐**。

#### Supabase

[Sign in with Google（Supabase Auth）](https://supabase.com/docs/guides/auth/social-login/auth-google) 要求配置 OAuth Client、回调 URI，Android 端通常经 Supabase Auth SDK 或 ID Token 交换 Supabase Session。这与当前 **纯 Credential Manager + DataStore** 路径不同，需：

- **方案 A**：迁移至 Supabase Auth（替换 `AuthRepositoryImpl` 会话层，统一 JWT）
- **方案 B**：Custom Access Token JWT + RLS（[Auth Hooks](https://supabase.com/pricing) 支持，但配置复杂）

**结论**：Firebase 与现有 Credential Manager 的官方衔接更顺；Supabase 需更大 Auth 重构。

| | Firebase | Supabase |
|---|----------|----------|
| 保留 Credential Manager UI | ✅ 官方支持 | ⚠️ 需自行桥接 ID Token |
| 保留 `google:{sub}` 业务 ID | ✅ 文档字段即可 | ✅ 可映射至 `auth.users` metadata |
| 安全规则可用的 auth 上下文 | 需加 Firebase Auth（薄层） | 需 Supabase Auth 或自定义 JWT |
| 额外服务端 | 不需要 | Custom JWT 时可能需要 Edge Function |

---

### 2. Room local-first + 同步架构

项目 ADR 要求 **Room 为唯一真相源**；v5 是在 Repository 插入 `RemoteDataSource`，而非替换 Room。

#### Firebase — Firestore

[Firestore 离线持久化](https://firebase.google.com/docs/firestore/manage-data/enable-offline) 在 **Android 默认启用**：客户端自动缓存活跃数据，离线可读写，联网后同步（同文档多写为 last-write-wins）。可与 Room 并存：

```
UI → ViewModel → Repository
                    ├── Room DAO（SSOT，现有）
                    └── FirestoreRemote（sync worker 双向同步）
```

Firestore SDK 自带离线缓存，可作为 sync 的辅助层；Repository 仍以 Room Flow 驱动 UI（与 v4 一致）。

#### Supabase — Postgres + Realtime

[Supabase Realtime](https://supabase.com/docs/guides/realtime) 提供 Postgres Changes / Broadcast，适合聊天与订单状态推送。[Postgres Changes 文档](https://supabase.com/docs/guides/realtime/postgres-changes) 通过 WAL 监听表变更。

**Supabase 官方 Kotlin 客户端（[supabase-kt](https://github.com/supabase-community/supabase-kt)）不提供类似 Firestore 的内建离线持久化**。离线优先需：

- 继续以 **Room 为唯一本地缓存**（本项目本来如此），或
- 引入第三方 sync 引擎（如 [PowerSync](https://docs.powersync.com/integrations/supabase/guide.md)——额外依赖与学习成本）

[Supabase 官方 Quickstart（Kotlin）](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin) 示例为在线 `LaunchedEffect` 拉取，未覆盖离线队列。

**结论**：两者在「Room SSOT + Remote sync」架构下均可行；Firebase 额外提供客户端离线缓存，降低 sync 层部分复杂度。Supabase 的 SQL 表结构与 Room Entity 更接近，但 **sync 层工作量相当**，不构成决定性优势。

| | Firebase | Supabase |
|---|----------|----------|
| 与 Room SSOT 兼容 | ✅ Repository seam | ✅ Repository seam |
| 客户端内建离线 | ✅ Android 默认开启 | ❌ 需 Room 或 PowerSync |
| 数据模型 | NoSQL 文档 | SQL 表（更接近 Room） |
| 实时更新 | Snapshot 监听 | Realtime Postgres Changes |

---

### 3. 实时聊天 / 私信

v5 首要远端化域（[ADR-0006](../adr/0006-chat-local-first-v3.md)、[baas-backend-options.md](baas-backend-options.md)）。

| | Firebase | Supabase |
|---|----------|----------|
| 机制 | Firestore `addSnapshotListener` | Realtime `postgres_changes` 或 Broadcast |
| 离线发消息 | SDK 队列 + Room 草稿（现有） | Room 草稿 + 联网后 PostgREST insert |
| 与 ADR-0006 一致度 | **高**（已点名 Firestore） | 中（需新 ADR） |
| Android 示例丰富度 | 高（Firebase 官方） | 中（社区 supabase-kt） |

Firestore 对「会话 / 消息」集合的 snapshot 监听是移动端聊天常见模式；Supabase Realtime 同样可行，但 Android 端示例与 ADR 预设偏向 Firebase。

---

### 4. 挂牌图片云存储

| | Firebase Storage | Supabase Storage |
|---|------------------|------------------|
| Android 上传 | [官方 Upload 文档](https://firebase.google.com/docs/storage/android/upload-files) | [Storage 指南](https://supabase.com/docs/guides/storage)（REST / SDK） |
| 访问控制 | [Firebase Security Rules](https://firebase.google.com/docs/storage/security)（与 Firestore 共享 auth） | RLS + Storage policies |
| 免费额度 | Spark：legacy bucket 5 GB 存储等（[定价](https://firebase.google.com/pricing)） | Free：1 GB 文件存储（[定价](https://supabase.com/pricing)） |
| 与 Coil 集成 | 下载 URL / gs:// → HTTPS | Public / signed URL |

两者均满足挂牌图上传；Firebase 与 Firestore 共用 `request.auth`，规则心智统一。

---

### 5. Push 通知（FCM）

v3 #29、v4 #41 均将 FCM 标为 **v5+ 候选**。

| | Firebase | Supabase |
|---|----------|----------|
| FCM | **原生、免费**（[FCM Android 文档](https://firebase.google.com/docs/cloud-messaging/android/client)） | 无原生封装 |
| 实现路径 | Cloud Functions / 客户端触发 + FCM SDK | [Edge Functions](https://supabase.com/pricing) + 自行调用 FCM HTTP v1 |
| 学习成本 | 低（同一 Firebase 项目） | 中高（额外 Server 逻辑） |

对本项目，**新消息 / 订单状态 Push** 是 v5 核心能力；Firebase 显著占优。

---

### 6. Android 学习曲线与生态

| | Firebase | Supabase |
|---|----------|----------|
| 官方 Android 文档 | [Firebase Android 设置](https://firebase.google.com/docs/android/setup) 完整 | [Kotlin Quickstart](https://supabase.com/docs/guides/getting-started/quickstarts/kotlin) 有，深度示例较少 |
| 客户端库 | Google 维护 `firebase-bom` | 社区 [supabase-community/supabase-kt](https://github.com/supabase-community/supabase-kt)（~844 stars） |
| 与 Jetpack / Hilt | 大量官方与社区范例 | 需自行封装 Repository |
| Agent / IDE 支持 | Firebase agent skills 官方维护 | Supabase agent-skills 可选（Quickstart 提及） |

作为 **Android 学习项目**，Firebase 文档与 tooling 更成熟；supabase-kt 可用但维护方为非官方 community org。

---

### 7. 安全模型

| | Firestore Security Rules | Supabase RLS |
|---|--------------------------|--------------|
| 模型 | 文档路径 + `request.auth` 条件 | SQL 行级 `USING` / `WITH CHECK` |
| 与 Room 关系表 | 需映射为文档/子集合 | 可直接镜像 Postgres 表 |
| 学习曲线 | 规则语言专用 | SQL 策略，与 Room 思维一致 |
| 本项目 | 需 Firebase Auth 上下文 | 需 Supabase Auth JWT |

两者均能满足「买家只能读自己的订单、卖家只能改自己的挂牌」；Firebase 路径与已选的 Google 登录薄层集成更短。

---

### 8. 成本（单人学习项目）

#### Firebase Spark（免费）

来源：[Firebase Pricing](https://firebase.google.com/pricing)

- Firestore：1 GiB 存储；50K 读 / 20K 写 / 天
- Storage：5 GB（legacy bucket 等条件）
- **FCM：免费**
- Authentication（非 Phone）：免费

对个人练习与 smoke 测试通常足够；超出需 Blaze 按量计费。

#### Supabase Free

来源：[Supabase Pricing](https://supabase.com/pricing)

- 500 MB 数据库；**1 GB** 文件存储
- Realtime：200 并发连接；2M 消息 / 月
- **1 周无活动会 pause 项目**（需注意 CI / 长期 demo）
- Edge Functions：500K 调用 / 月（FCM 桥接消耗此额度）

学习阶段两者均可免费起步；Supabase 的 **inactivity pause** 对长期挂机 demo 略烦；Firebase Spark 无此限制。

---

### 9. _vendor lock-in 与迁移_

| | Firebase | Supabase |
|---|----------|----------|
| 锁定程度 | 高（Firestore + FCM + Storage 一体） | 中（Postgres 标准 SQL，[可自托管](https://supabase.com/pricing)） |
| 迁移成本 | 导出 + 重写 sync 层 | pg_dump + 换客户端 |
| 对本项目 | 学习项目可接受；Repository 抽象已预留 | 开源友好，但 v5 实现成本更高 |

项目已通过 `RemoteDataSource` seam（[ADR-0010](../adr/0010-defer-baas-backend-to-v5-plus.md)）降低切换成本；短期选型应优先 **交付 v5 功能**，而非理论可迁移性。

---

## 决策矩阵（汇总）

| 维度 | Firebase | Supabase | 对本项目权重 |
|------|----------|----------|--------------|
| 与 Credential Manager / v3.2 Auth | ⭐⭐⭐⭐ | ⭐⭐ | 高 |
| Room SSOT + sync | ⭐⭐⭐⭐ | ⭐⭐⭐ | 高 |
| 实时聊天 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 高 |
| 挂牌图片 Storage | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 中 |
| FCM Push | ⭐⭐⭐⭐⭐ | ⭐⭐ | **高** |
| Android 官方文档 / 示例 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 高 |
| SQL 与 Room 模型一致 | ⭐⭐ | ⭐⭐⭐⭐⭐ | 中 |
| 免费 tier 友好度 | ⭐⭐⭐⭐ | ⭐⭐⭐ | 中 |
| ADR 既有倾向 | ⭐⭐⭐⭐⭐（ADR-0006） | ⭐⭐ | 中 |
| 自托管 / 开源 | ⭐⭐ | ⭐⭐⭐⭐⭐ | 低 |

---

## Recommendation（结论）

###  verdict：**推荐 Firebase（Firestore + Firebase Storage + FCM）**

**理由（按优先级）**：

1. **FCM 是 v5 刚需**，Firebase 原生免费集成；Supabase 需 Edge Function 拼装，偏离 Android 学习主线。
2. **Credential Manager 已有实现**可与 [Firebase Auth Google 登录](https://firebase.google.com/docs/auth/android/google-signin) 官方路径合并，Auth 改动小于迁移 Supabase Auth。
3. **ADR-0006 已预设 Firestore** 为聊天同步首选；延续决策降低 v5 Map 讨论成本。
4. **Firestore Android 离线持久化**默认开启，与 Room SSOT 互补，利于聊天 offline 体验。
5. **Android 官方文档与 BOM 依赖管理**更适合本仓库「Kotlin + Compose 学习」定位。

### Supabase 何时更值得选？

- 团队 **强依赖 SQL / Postgres**，希望远端表与 Room 1:1 镜像且愿意投入 sync 与 Auth 重构；
- 需要 **自托管** 或避免 Google 生态锁定；
- **暂不实现 FCM**，或可接受 Edge Function 维护成本。

以上均 **不符合** 当前 C2C Market v5 目标（跨设备聊天 + Push + 低运维学习项目）。

---

## 推荐 v5 集成顺序（Firebase 路径）

与 [baas-backend-options.md](baas-backend-options.md) 一致，细化为：

| 阶段 | 内容 | 主要官方参考 |
|------|------|--------------|
| **v5.0** | Firebase 项目、`google-services.json`、Firebase Auth（Credential Manager 薄层）、ADR-0011、`RemoteDataSource` 接口 | [Android Setup](https://firebase.google.com/docs/android/setup) · [Google Sign-In](https://firebase.google.com/docs/auth/android/google-signin) |
| **v5.1** | Firestore 同步会话 / 消息；Security Rules 绑定 `google:{sub}` | [Firestore 离线](https://firebase.google.com/docs/firestore/manage-data/enable-offline) · [Security Rules](https://firebase.google.com/docs/firestore/security/get-started) |
| **v5.2** | Storage 上传挂牌图；Room 存 HTTPS URL | [Storage Upload](https://firebase.google.com/docs/storage/android/upload-files) |
| **v5.3** | FCM：新消息、订单状态变更；Deep Link 跳转 | [FCM Android](https://firebase.google.com/docs/cloud-messaging/android/client) |
| **v5.4** | Firestore 同步挂牌 / 订单状态 | 同上 |
| **v5.5–v5.6** | 测试、Release、`release-smoke-v5.md` | [ADR-0008](../adr/0008-release-r8-and-ci.md) |

Repository 结构（延续 ADR-0010）：

```
ChatRepository / ListingRepository / OrderRepository
    ├── local: Room DAO（SSOT，UI 只读此 Flow）
    └── remote: FirestoreRemoteDataSource
         └── SyncWorker：Room ↔ Firestore，冲突策略 last-write-wins + 领域事件
```

---

## ADR-0011 草案要点（若接受 Firebase）

- **接受** Firebase 作为 v5 BaaS：Firestore（数据）、Storage（图片）、FCM（Push）、Firebase Auth（安全层 only）。
- **保留** Room 为 SSOT；Firestore 为远端副本 + 跨设备同步源。
- **保留** 业务 `userId = "google:{sub}"`；Firebase Auth uid 仅用于 Rules，或通过 custom claim 写入 `sub`。
- **拒绝** v5 引入 Supabase / 自建 Node 后端（范围控制）。
- **拒绝** 真支付、Play 上架流水线（仍 v5+ 或 out of scope）。

---

## 对 `baas-backend-options.md` 的更新建议

本文档为 **v5 选型结论**；原 [`baas-backend-options.md`](baas-backend-options.md) 可保留为候选概览，并在顶部增加：

```markdown
> v5 结论见 [`firebase-vs-supabase-v5.md`](firebase-vs-supabase-v5.md)（2026-09-21）：推荐 Firebase。
```

---

## 主要参考来源（Primary Sources）

| 来源 | URL |
|------|-----|
| Firestore 离线持久化 | https://firebase.google.com/docs/firestore/manage-data/enable-offline |
| Firestore Security Rules | https://firebase.google.com/docs/firestore/security/get-started |
| Firebase Auth + Google（Credential Manager） | https://firebase.google.com/docs/auth/android/google-signin |
| Firebase Custom Auth | https://firebase.google.com/docs/auth/android/custom-auth |
| Firebase Storage Upload（Android） | https://firebase.google.com/docs/storage/android/upload-files |
| FCM Android 入门 | https://firebase.google.com/docs/cloud-messaging/android/client |
| Firebase 定价 | https://firebase.google.com/pricing |
| Supabase Kotlin Quickstart | https://supabase.com/docs/guides/getting-started/quickstarts/kotlin |
| Supabase Realtime | https://supabase.com/docs/guides/realtime |
| Supabase Postgres Changes | https://supabase.com/docs/guides/realtime/postgres-changes |
| Supabase Google 登录 | https://supabase.com/docs/guides/auth/social-login/auth-google |
| Supabase Storage | https://supabase.com/docs/guides/storage |
| Supabase 定价 | https://supabase.com/pricing |
| supabase-kt（社区客户端） | https://github.com/supabase-community/supabase-kt |
| 项目 ADR-0006 / ADR-0010 | `docs/adr/` |

---

## 下一步

1. 开 **v5 Map Issue**（对标 #41），首票 `wayfinder:research` 引用本文档。
2. 撰写 **ADR-0011** 正式接受 Firebase。
3. v5.0 Spike：`FirestoreRemoteDataSource` + 单会话 sync POC（双设备验证）。
