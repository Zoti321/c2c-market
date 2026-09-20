# C2C Market MVP UI 原型

> **关联 Issue**：[设计商品列表、详情与购物车 UI 布局](https://github.com/Zoti321/c2c-market/issues/7)  
> **规格依据**：[锁定 MVP 功能边界与验收标准](https://github.com/Zoti321/c2c-market/issues/5)  
> **设计系统**：[`design-system/c2c-market/MASTER.md`](../../design-system/c2c-market/MASTER.md)（ui-ux-pro-max 生成）

## 设计方向（ui-ux-pro-max 摘要）

| 维度 | 选型 |
|------|------|
| 产品模式 | **Marketplace / Directory** — 分类 + 商品列表（MVP 无搜索栏） |
| 风格 | **Flat Design** — 低 elevation、无重阴影、150–200ms 状态过渡 |
| 主色 | `#2563EB` 信任蓝 — TopAppBar、Chip、Outlined 按钮 |
| 强调/价格/CTA | `#16A34A` 转化绿 — 价格、加入购物车、去结算 |
| 背景 | `#EFF6FF` light / Material dark 自适应 |
| 正文 | `#0F172A` on white（≥4.5:1 对比） |
| 图标 | **Material Icons Extended**（Android 原生；MASTER 禁止 emoji 作结构图标） |
| 字体 | **Material 3 默认 Typography**（MVP 不引入 Google Fonts；MASTER 建议 Inter 留 v2） |

### MVP 对 MASTER 的裁剪

| MASTER 建议 | MVP 决定 |
|-------------|----------|
| Hero 搜索为主 CTA | ❌ 无搜索；首屏即商品网格 |
| List your item | ❌ v2.2 挂牌 |
| Trust/Safety 区块 | ✅ 首页底部署名「商品数据来自 FakeStore」 |
| Web hover transform | ❌ Compose 用 `clickable` + ripple |

### 页面 Override 文件

| 页面 | 文件 |
|------|------|
| 首页 | [`design-system/c2c-market/pages/home.md`](../../design-system/c2c-market/pages/home.md) |
| 商品详情 | [`design-system/c2c-market/pages/product-detail.md`](../../design-system/c2c-market/pages/product-detail.md) |
| 购物车 | [`design-system/c2c-market/pages/cart.md`](../../design-system/c2c-market/pages/cart.md) |

---

## Material 3 Token 映射（implement 写入 `Color.kt` / `Theme.kt`）

**Light `colorScheme`（关闭 dynamicColor，使用固定 marketplace 主题）：**

| Token | Hex | 用途 |
|-------|-----|------|
| `primary` | `#2563EB` | TopAppBar、导航、Outlined 按钮 |
| `onPrimary` | `#FFFFFF` | — |
| `secondary` | `#3B82F6` | 次要强调 |
| `tertiary` | `#16A34A` | **价格、主 CTA**（加购/去结算） |
| `onTertiary` | `#FFFFFF` | CTA 文字 |
| `background` | `#EFF6FF` | 全屏背景 |
| `surface` | `#FFFFFF` | Card 表面 |
| `onSurface` | `#0F172A` | 正文 |
| `onSurfaceVariant` | `#475569` | 副文案、出处 |
| `error` | `#DC2626` | 错误、删除 |
| `outline` | `#BFDBFE` | 边框、Divider |

**Dark：** 同 hue 降亮度升对比（implement 阶段补全 `darkColorScheme`）。

```kotlin
// Theme.kt — MVP 建议
C2cmarketTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) { ... }
```

---

## 导航结构

```
MainActivity
└── Scaffold + BottomNav
    ├── home
    ├── category
    ├── cart
    └── profile

Push 全屏（无 BottomNav）：
    ├── product/{id}
    ├── checkout
    └── order/{id}
```

分类 Tab 内嵌 NavHost：`CategoryList` → `CategoryProductList` → `product/{id}`。

---

## 1. 首页 — 商品 Paging 网格

```
┌──────────────────────────────────────┐
│  TopAppBar: 「首页」                  │
├──────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐   │
│  │  [1:1 img]  │  │  [1:1 img]  │   │
│  │  Title      │  │  Title      │   │
│  │  $109.95    │  │  $22.30     │   │  ← tertiary green
│  │  [electronics]│ │ [men's...] │   │
│  └─────────────┘  └─────────────┘   │
│         … Paging …                   │
│  商品数据来自 FakeStore               │  labelSmall muted
└──────────────────────────────────────┘
│ [首页][分类][购物车][我的]            │
└──────────────────────────────────────┘
```

---

## 2. 商品详情

```
┌──────────────────────────────────────┐
│  [←]  商品详情                        │
├──────────────────────────────────────┤
│  [ Hero Image 4:3 ]                  │
│  $109.95          ← tertiary green   │
│  Title                               │
│  [category]  ★ 3.9 (120)             │
│  ─────────────────                   │
│  描述正文…                            │
├──────────────────────────────────────┤
│ [♡ 收藏]          [加入购物车]        │  Outlined / accent Button
└──────────────────────────────────────┘
```

---

## 3. 分类 Tab

- 垂直 `ListItem` + leading 图标 + chevron（四分类）
- 分类商品区 **复用首页 ProductCard 网格**

---

## 4. 购物车

```
┌──────────────────────────────────────┐
│  TopAppBar: 「购物车」                │
├──────────────────────────────────────┤
│  [img] Title                         │
│        $109.95  [－] 2 [＋]  [Delete] │
├──────────────────────────────────────┤
│  合计：$219.90                       │
│  [        去结算        ]            │  accent Button
└──────────────────────────────────────┘
```

Empty：`ShoppingCartOutlined` + 「购物车是空的」+ `TextButton`「去首页逛逛」

---

## 5. 结算 / 我的（implement 参考）

- **结算**：确认列表 + 合计 +「游客模式 · 无需支付」+ accent「确认下单」
- **我的**：游客 Banner + 订单 Card 列表

---

## 共用 Composable

| 名称 | 用途 |
|------|------|
| `ProductCard` | 首页 + 分类网格 |
| `ProductGrid` | Paging + LazyVerticalGrid |
| `PriceText` | `$%.2f`，tertiary 色 |
| `LoadingContent` / `ErrorContent` / `EmptyContent` | 三态壳 |
| `QuantityStepper` | 购物车 |

---

## 无障碍（ui-ux-pro-max + MVP）

- 商品卡片 `contentDescription = "{title}, {price}"`
- Error：`liveRegion = Polite`（Compose semantics）
- Loading：`progressBarRangeInfo` 或 Text「加载中」
- Empty：文案 + 可操作 `TextButton`（非空白屏）
- 触摸目标 ≥ **48dp**
- 深色模式独立验证对比度

---

## 决策锁定表

| 问题 | 决定 |
|------|------|
| 设计系统 | ui-ux-pro-max Marketplace + Flat；见 MASTER |
| 主题 | 固定蓝绿 marketplace token；**dynamicColor = false** |
| 首页 | 2 列网格 + FakeStore 出处 |
| 详情 | Hero + 绿价 + 底栏加购/收藏 |
| 分类 | ListItem → 同款网格 |
| 购物车 | 步进器 + accent 去结算 |
| 导航 | 详情/结算 push 全屏 |
