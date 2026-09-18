# C2C Market（原生 Android 学习项目）

> 轻量 C2C 电商 / 二手交易 App，边做边学，目标初工程师水平。

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

### MVP（当前阶段）

- [x] 项目骨架：Hilt + Theme + NavHost + Bottom Nav（4 Tab）
- [ ] 首页 Paging 3 + FakeStore 集成
- [ ] 分类 Tab
- [ ] 商品详情 + Coil
- [ ] Room：购物车 + 收藏
- [ ] 结算 + Mock 订单
- [ ] 订单列表/详情 + Loading/Error/Empty + 深色模式
- [ ] MVP 验收

**MVP 明确不做**：登录、真支付、发布挂牌、聊天、Push、地图

### v2 扩展

- v2.1 搜索 + 浏览历史
- v2.2 发布挂牌（相册选图 + 本地 URI）
- v2.3 地址 CRUD
- v2.4 WorkManager + 通知
- v2.5 测试
- v2.6 Release + R8

### v3（可选）

- 私信/聊天（Room 本地优先，Firebase 可选）
- Google Sign-In
- 地图 / Deep Link

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
- JDK 17
- Android SDK 37
- 模拟器或真机（需联网）

## 快速开始

```bash
# 在项目根目录执行
./gradlew assembleDebug

# 安装到已连接设备
./gradlew installDebug
```

> FakeStore **无需 API Key**，确保设备可访问互联网即可。

## 参考资源

- [FakeStore API](https://fakestoreapi.com/)
- [Jetpack Compose 官方教程](https://developer.android.com/jetpack/compose/tutorial)
- [Android 架构指南](https://developer.android.com/topic/architecture)
- 架构参考（只读，不迁移代码）：`E:\projects\android_native\app1`

## 许可证

本项目仅供个人学习使用。
