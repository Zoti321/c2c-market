# v3 聊天采用 local-first（Firebase 可选）

C2C 场景下买卖双方私信是 v3 能力，MVP/v2 不做。决策预先锁定：消息以 Room 为本地真相源，支持离线草稿与已读状态；远端同步可选 Firebase Firestore 或自建后端，不在 MVP 范围实现。UI 与 Repository 接口设计时预留 `Conversation` / `Message` 领域模型，但不提前引入 Firebase 依赖。

**Status**: accepted (v3 scope)

**Considered options**: Room local-first + 可选 Firebase（选用）、纯 Firebase Realtime（暂缓，离线体验差）、WebSocket 自建（拒绝，v3 复杂度过高）
