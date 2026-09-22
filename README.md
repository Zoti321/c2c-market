# C2C Market（原生 Android 学习项目）

> **路线图**：[C2C Market v5 — 路线图 (#49)](https://github.com/Zoti321/c2c-market/issues/49) · v4 已交付 ([#41](https://github.com/Zoti321/c2c-market/issues/41) · `4.0.0`)

通过开发一个买家闭环的 C2C 市场应用，系统学习原生 Android 开发。项目基于 **Kotlin + Jetpack Compose + Material 3 + Hilt**，逐步引入网络请求、Paging、Room 本地存储与 MVVM 架构分层。

## 项目目标

- 调用 [FakeStore API](https://fakestoreapi.com/) 展示商品列表与详情
- 实现买家闭环：首页 + 分类 + 购物车 + 我的
- 掌握 Jetpack Compose、Navigation、Paging 3、Room、Coil
- 实践 `Screen → ViewModel → Repository → (Remote | Room)` 分层
- Mock 下单流程（无真支付、无登录）

## 技术栈

| 类别 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 + Navigation Compose |
| 架构 | MVVM + Hilt (KSP) |
| 网络 | Retrofit + OkHttp + kotlinx.serialization |
| 本地 | Room（购物车、收藏、订单）+ DataStore（偏好） |
| 列表 | Paging 3 |
| 图片 | Coil |
| 异步 | Kotlin Coroutines + Flow / StateFlow |
| 最低 SDK | 26（Android 8.0） |
| JDK | 17 |

## 功能范围

### MVP（已交付）

- [x] 项目骨架：Hilt + Theme + NavHost + Bottom Nav（4 Tab）
- [x] 决策票：FakeStore 调研、MVP 边界、UI 原型、各模块规格（[#1](https://github.com/Zoti321/c2c-market/issues/1)）
- [x] 买家闭环：首页 Paging → 分类 → 详情 → Room 购物车/收藏 → Mock 结算 → 订单（PR #18）
- [x] [MVP 验收 (#11)](https://github.com/Zoti321/c2c-market/issues/11)

规格见 [锁定 MVP 功能边界与验收标准](https://github.com/Zoti321/c2c-market/issues/5)（Issue comment 全文）。

### v2（已交付）

- [x] v2.1 搜索 + 浏览历史（[约定 v2.1 搜索与浏览历史规格](https://github.com/Zoti321/c2c-market/issues/21)）
- [x] v2.2 发布挂牌（[约定 v2.2 发布挂牌规格](https://github.com/Zoti321/c2c-market/issues/22)）
- [x] v2.3 地址 CRUD（[约定 v2.3 收货地址 CRUD 与结算集成规格](https://github.com/Zoti321/c2c-market/issues/23)）
- [x] v2.4 WorkManager + 通知（[约定 v2.4 WorkManager 与本地通知规格](https://github.com/Zoti321/c2c-market/issues/24)）
- [x] v2.5 测试（[约定 v2.5 测试范围与策略规格](https://github.com/Zoti321/c2c-market/issues/25)）
- [x] v2.6 Release + R8（[约定 v2.6 Release、R8 与 CI 规格](https://github.com/Zoti321/c2c-market/issues/26)）

### v3（已交付）

**路线图**（[#29](https://github.com/Zoti321/c2c-market/issues/29)）**已合入 `main`**（`3.0.0`，PR #40）。

- [x] v3.1 私信 / 聊天（[约定 v3.1 私信与聊天规格](https://github.com/Zoti321/c2c-market/issues/30)）
- [x] v3.2 Google Sign-In（[约定 v3.2 Google Sign-In 与账号规格](https://github.com/Zoti321/c2c-market/issues/31)）
- [x] v3.3 Deep Link / 地图（[约定 v3.3 Deep Link 与地图规格](https://github.com/Zoti321/c2c-market/issues/32)）
- [x] v3.4 测试（[约定 v3.4 测试范围与策略规格](https://github.com/Zoti321/c2c-market/issues/33)）
- [x] v3.5 Release + R8（[约定 v3.5 Release、R8 与 CI 规格](https://github.com/Zoti321/c2c-market/issues/34)）

### v4（已交付）

**路线图**（[#41](https://github.com/Zoti321/c2c-market/issues/41)）**已合入 `main`**（`4.0.0`）。Release 验收见 [`docs/release-smoke-v4.md`](docs/release-smoke-v4.md)。

- [x] v4.1 卖家私信收件箱 + 双向聊天（[约定 v4.1 规格](https://github.com/Zoti321/c2c-market/issues/42)）
- [x] v4.2 挂牌状态机 + 卖家订单视图（[约定 v4.2 规格](https://github.com/Zoti321/c2c-market/issues/43)）
- [x] v4.3 订单生命周期 + 面交确认流（[约定 v4.3 规格](https://github.com/Zoti321/c2c-market/issues/44)）
- [x] v4.4 收藏列表 + Cart/Favorites 用户隔离（[约定 v4.4 规格](https://github.com/Zoti321/c2c-market/issues/45)）
- [x] v4.5 测试（[约定 v4.5 测试范围与策略规格](https://github.com/Zoti321/c2c-market/issues/46)）
- [x] v4.6 Release + R8（[约定 v4.6 Release、R8 与 CI 规格](https://github.com/Zoti321/c2c-market/issues/47)）

### v5（已交付）

**路线图**（[#49](https://github.com/Zoti321/c2c-market/issues/49)）**已合入 `feature/v5`**（**5.0.0**）。Release 验收见 [`docs/release-smoke-v5.md`](docs/release-smoke-v5.md)。

- [x] 调研 Firebase vs Supabase（[#50](https://github.com/Zoti321/c2c-market/issues/50)）
- [x] v5.0 Firebase 基线 + ADR-0011（[#51](https://github.com/Zoti321/c2c-market/issues/51)）
- [x] v5.1 聊天 Firestore 同步（[#52](https://github.com/Zoti321/c2c-market/issues/52)）
- [x] v5.2 挂牌图片 Storage（[#53](https://github.com/Zoti321/c2c-market/issues/53)）
- [x] v5.3 FCM Push + Deep Link（[#54](https://github.com/Zoti321/c2c-market/issues/54)）
- [x] v5.4 挂牌 / 订单 Firestore 同步（[#55](https://github.com/Zoti321/c2c-market/issues/55)）
- [x] v5.5 测试（[#56](https://github.com/Zoti321/c2c-market/issues/56)）
- [x] v5.6 Release + R8（[#57](https://github.com/Zoti321/c2c-market/issues/57)）

**Implement 顺序**：v5.0 → v5.1 → v5.2 → v5.3 → v5.4 + v5.5 测试 + v5.6 Release **同 PR**。

领域术语见 [`CONTEXT.md`](CONTEXT.md)；架构决策见 [`docs/adr/`](docs/adr/)。

## 架构设计

**约束**：UI **不得**直连 Retrofit 或 DAO；购物车、收藏、订单以 **Room 为 SSOT**；FakeStore 仅作商品远程源。

```
┌─────────────────────────────────────┐
│  UI (Compose Screen)                │
│  Home · Category · Cart · Profile   │
└──────────────┬──────────────────────┘
               │ collect StateFlow
┌──────────────▼──────────────────────┐
│  ViewModel                          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Repository                         │
└──────────────┬──────────────────────┘
               │
       ┌───────┴───────┐
┌──────▼──────┐ ┌──────▼──────┐
│ FakeStoreApi│ │  Room DAO   │
│  (Remote)   │ │  (Local)    │
└─────────────┘ └─────────────┘
```

### 目标包结构

```
com.zoti321.c2cmarket/
├── C2CApplication.kt
├── MainActivity.kt
├── di/           NetworkModule, DatabaseModule, RepositoryModule
├── ui/           navigation, theme, home, product, cart, checkout, order, profile
└── data/         local, remote, repository, model
```

## 公开 API — FakeStore

Base URL: `https://fakestoreapi.com/`

| 端点 | 用途 |
|------|------|
| `GET /products` | 商品列表 |
| `GET /products/{id}` | 商品详情 |
| `GET /products/categories` | 分类列表 |
| `GET /products/category/{name}` | 按分类筛选 |

> FakeStore 无真实 C2C 语义（无卖家、无私信）；挂牌与聊天靠本地 Mock，见 ADR。

## 环境要求

- Android Studio（推荐最新稳定版）
- **JDK 17**（Gradle Daemon 与 CI 均使用 17；仓库根目录有 `.java-version`）
- Android SDK 37
- 模拟器或真机（需联网）

## 本地开发与验证

> Gradle Daemon 请使用 **JDK 17**（与 CI 一致；见 `.java-version`）。在 JDK 21+ 上 Detekt 可能无法运行。

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test                              # JVM 单测 + Compose UI 冒烟（Robolectric）
./gradlew lintDebug detekt                  # 静态分析（与 CI 一致）
./gradlew jacocoDebugUnitTestReport jacocoDebugUnitTestCoverageVerification
./gradlew assembleRelease                   # Release + R8（需 keystore.properties）
```

Release 本地配置（均 gitignore，见 [`keystore.properties.example`](keystore.properties.example)、[`local.properties.example`](local.properties.example)）。

### Firebase 本地 setup（v5 远端功能）

1. 在 [Firebase Console](https://console.firebase.google.com/) 创建项目
2. 添加 Android App（包名 `com.zoti321.c2cmarket`），下载 `google-services.json` → 复制到 `app/google-services.json`（见 [`app/google-services.json.example`](app/google-services.json.example)）
3. `local.properties` 配置 `google.web_client_id`（与 Console OAuth Web Client 一致）
4. 可选：`firebase deploy --only functions,firestore:rules,storage`（Cloud Functions 见 [`functions/`](functions/)）
5. 游客模式可用 FakeStore / 本地功能；**聊天 sync、云图片、FCM、挂牌/订单 sync 需 Google 登录**

> FakeStore **无需 API Key**，确保设备可访问互联网即可。

## 参考资源

- [FakeStore API](https://fakestoreapi.com/)
- [Jetpack Compose 官方教程](https://developer.android.com/jetpack/compose/tutorial)
- [Android 架构指南](https://developer.android.com/topic/architecture)
- 架构参考（只读，不迁移代码）：`E:\projects\android_native\app1`

## 许可证

本项目仅供个人学习使用。
