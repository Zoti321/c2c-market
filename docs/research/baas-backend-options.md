# BaaS 后端选型调研（v5+ 候选）

> **状态**：调研草案 · v4 不引入远端后端  
> **关联**：ADR-0006（聊天 local-first）、ADR-0010（v4 仍 Room SSOT）、路线图 [#41](https://github.com/Zoti321/c2c-market/issues/41)

v3 完成买家私信、Google 登录与 Deep Link；v4 聚焦**卖家闭环 + 订单状态机**（仍 local-first）。本文档为 **v5+** 引入远端同步、Push、跨设备账号等能力时的选型参考。

## 背景与约束

| 约束 | 说明 |
|------|------|
| 学习项目 | 优先官方文档与社区示例；控制运维成本 |
| 现有架构 | Room SSOT + Repository 抽象；Google Sign-In 已用 Credential Manager（非 Firebase Auth） |
| v4 边界 | **不**在 v4 引入 Firebase / Supabase 依赖；v4 实现时 Repository 接口预留 `RemoteDataSource`  seam |
| 需远端化的域 | 聊天消息、挂牌元数据、订单状态、图片上传（挂牌图目前为本地 URI） |

## 候选方案对比

### Firebase（Google 生态）

| 能力 | 产品 | 与现有代码关系 |
|------|------|----------------|
| 文档库 + 实时监听 | Firestore | ADR-0006 已列为聊天同步首选；可扩至挂牌/订单 |
| 文件存储 | Firebase Storage | 替代挂牌本地 URI，支持跨设备 |
| Push | FCM | v3 #29 Out of scope → v5+ 自然候选 |
| 身份 | Firebase Auth | **与 v3.2 冲突**（已选 Credential Manager + 本地 userId）；可仅用 Firestore Security Rules 绑定 `google:{sub}` 声明 |

**优点**：Android 官方集成成熟；Firestore 离线缓存与 Room 可并存（local-first + sync）；FCM 与 Google 登录同控制台。

**缺点**：NoSQL 建模与 Room 关系表需双写或 sync 层；Vendor lock-in；免费额度外计费需关注。

**推荐接入顺序（若选 Firebase）**：

1. Firestore 同步 **会话 / 消息**（替换 Mock 自动回复、支持跨设备）
2. Storage 上传 **挂牌图片**
3. FCM **新消息 / 订单状态** Push
4. （可选）Firestore 同步挂牌与订单状态

### Supabase（Postgres + 开源 BaaS）

| 能力 | 产品 | 与现有代码关系 |
|------|------|----------------|
| 关系型 API | PostgREST + Postgres | 表结构可更接近 Room Entity，迁移心智负担较低 |
| 实时 | Realtime subscriptions | 聊天、订单状态推送 |
| 文件 | Storage | 挂牌图片 |
| 身份 | Supabase Auth（Google OAuth） | 可与现有 Google Sign-In **并行评估**（需统一 userId 映射） |
| Push | 无原生 FCM 封装 | 需 Edge Functions + FCM 或第三方 |

**优点**：SQL/关系模型与学习 Room 一致；自托管可选；REST/Realtime SDK 清晰。

**缺点**：Android 生态示例少于 Firebase；Push 需额外拼装；Google OAuth 与 Credential Manager 集成需自行对接 JWT。

**推荐接入顺序（若选 Supabase）**：

1. Postgres 表：`conversations`、`messages`、`listings`、`orders`
2. Realtime 订阅消息与订单状态
3. Storage + 挂牌图片 URL
4. Edge Function 发 FCM（若需要 Push）

## 决策矩阵（摘要）

| 维度 | Firebase | Supabase |
|------|----------|----------|
| 与 ADR-0006 一致度 | 高（已点名 Firestore） | 中（需新 ADR） |
| 与 v3.2 Google 登录 | 可不改 Auth，Rules 绑 sub | 需 Auth 或自定义 JWT |
| Room 共存 | Firestore 离线 + Repository sync | Retrofit/Supabase-KT + Room cache |
| 聊天实时 | 原生 snapshot 监听 | Realtime channel |
| 运维 | 托管，零服务器 | 托管或自托管 |
| 学习曲线（Android） | 较低 | 中等 |

## v4 对 v5+ 的预留建议

implement v4 时保持以下 seam，降低后续接入成本：

```
Repository (interface)
    ├── local: Room DAO
    └── remote: NoOpRemoteDataSource  // v4
                 └── Future: FirestoreRemote / SupabaseRemote
```

- **ChatRepository**：`sendMessage` / `observeMessages` 经单一入口，便于插入 sync worker
- **ListingRepository / OrderRepository**：状态变更 emit 领域事件，便于 v5 上报远端
- **userId**：继续 `"google:{sub}"` 字符串，远端文档/行主键与之一致

## 下一步（v5 路线图启动前）

1. 在 v4 grilling 各票中确认 **不引远端** 的边界
2. v4 交付后开 **v5 Map Issue**，子票：`wayfinder:research` 复评本文档 + Spike（Firestore vs Supabase 最小 POC）
3. 选定方案后写 **ADR-0011**（接受具体 BaaS）并更新 CONTEXT.md

## 参考

- [Firebase Android 文档](https://firebase.google.com/docs/android/setup)
- [Supabase Kotlin 客户端](https://github.com/supabase-community/supabase-kt)
- 项目 ADR-0001（FakeStore 暂缓自建 Mock Server）、ADR-0006（聊天 local-first）
