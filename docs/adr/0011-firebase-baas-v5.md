# ADR-0011: Firebase BaaS for v5

**Status**: accepted (v5 scope)

## Context

v5 需要跨设备聊天、挂牌/订单同步、云图片与 Push，同时保留 Room 作为本地 SSOT。见 [#50](https://github.com/Zoti321/c2c-market/issues/50) 调研与 [#51–#57](https://github.com/Zoti321/c2c-market/issues/49) 规格。

## Decision

- **接受** Firebase 作为 v5 BaaS：Firestore（数据）、Storage（图片）、FCM（Push）、Firebase Auth（安全层 only）
- **保留** Room 为 SSOT；Firestore 为远端副本 + 跨设备同步源
- **保留** 业务 `userId = "google:{sub}"`；Firebase Auth uid 仅用于 Rules + `/userMappings`
- **拒绝** v5 引入 Supabase / 自建 Node 后端
- **拒绝** 废除 Room SSOT、Firestore 为唯一真相源
- **拒绝** 真支付、Play 上架流水线

## Architecture

```
UI → ViewModel → Repository
                    ├── Room DAO（SSOT，UI Flow 来源）
                    └── *RemoteDataSource（Firestore / Storage / NoOp）
```

Auth：`Credential Manager → Google ID Token → FirebaseAuth.signInWithCredential → DataStore userId`

## Consequences

- 远端功能需 Firebase 项目 + Google 登录
- CI 使用 `google-services.json` stub，不跑真实 Firebase 联调
- Cloud Functions 手动 `firebase deploy --only functions`
