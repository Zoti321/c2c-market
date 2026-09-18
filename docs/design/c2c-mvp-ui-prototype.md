# C2C Market MVP UI 原型

> **关联 Issue**：[设计商品列表、详情与购物车 UI 布局](https://github.com/Zoti321/c2c-market/issues/7)  
> **规格依据**：[`docs/spec/mvp-scope.md`](../spec/mvp-scope.md)  
> **设计方向**：Material 3 + 电商卡片网格；MVP 沿用 `dynamicColor`（Android 12+）与系统深色模式

## 设计方向摘要

| 维度 | 选型 |
|------|------|
| 模式 | **商品卡片网格** — 首页/分类商品列表 2 列；分类入口用**垂直列表** |
| 风格 | Material 3 `Card` + `TopAppBar`；无自定义重度 branding（学习优先） |
| 图标 | **Material Icons Extended**（与 Gradle 依赖一致）；禁止 emoji 作结构图标 |
| 导航 | Bottom Nav 四 Tab；**详情/结算 push 全屏**（隐藏 Bottom Nav） |
| 图片 | Coil `AsyncImage`，1:1 缩略图 + 详情 4:3 Hero |
| 价格 | `titleLarge` + **粗体**；货币 `$xx.xx`（FakeStore 美元） |

---

## 导航结构

```
MainActivity
└── Scaffold + BottomNav
    ├── home          （首页 Paging 网格）
    ├── category      （分类列表 → 分类商品网格）
    ├── cart          （购物车）
    └── profile       （我的 / 订单列表）
    
Push 全屏（无 BottomNav）：
    ├── product/{id}  （商品详情）
    ├── checkout      （结算确认）
    ├── order/{id}    （订单详情）
    └── category/{name}/products （可选：与 category 栈内嵌二选一，见下）
```

**分类导航（锁定）**：分类 Tab 内 `NavHost` 嵌套 ——  
`CategoryList` → push `CategoryProductList(category)` → push `ProductDetail`（与首页共用详情 composable）。

**详情导航（锁定）**：从首页或分类 **push** `product/{id}`，TopAppBar 返回键；Bottom Nav **不可见**。

---

## 1. 首页 — 商品 Paging 网格

### Success 线框

```
┌──────────────────────────────────────┐
│  TopAppBar: 「首页」                  │  centerAligned, 无搜索图标
├──────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐   │
│  │  [1:1 img]  │  │  [1:1 img]  │   │  LazyVerticalGrid columns=2
│  │  Title max2 │  │  Title max2 │   │  Card + clip 8dp
│  │  $109.95    │  │  $22.30     │   │  price titleMedium bold
│  │  [electronics]│ │ [men's...] │   │  AssistChip 分类
│  └─────────────┘  └─────────────┘   │
│         … Paging 加载更多 …          │
└──────────────────────────────────────┘
│ [首页][分类][购物车][我的]            │  BottomNav
└──────────────────────────────────────┘
```

| 元素 | 规范 |
|------|------|
| 网格 | `LazyVerticalGrid`，`GridCells.Fixed(2)`，gap 8dp，padding 16dp |
| 卡片 | `Card(onClick)`，`Column`：图 → 标题 → 价格 → 分类 Chip |
| 标题 | `bodyMedium`，`maxLines = 2`，`overflow = Ellipsis` |
| 分类 Chip | `AssistChip` 或 `SuggestionChip`，`labelSmall`，仅展示不可点 |
| 点击 | 导航 `product/{id}` |

### Loading / Error / Empty

- **Loading**：首屏网格区 `CircularProgressIndicator` 居中；Paging 底部 `CircularProgressIndicator` 行
- **Error**：`Icons.Default.CloudOff` + 文案 + `FilledTonalButton`「重试」
- **Empty**：`Icons.Default.Inventory2` + 「暂无商品」

---

## 2. 商品详情

### Success 线框

```
┌──────────────────────────────────────┐
│  [←]  商品详情                        │  TopAppBar + NavigationIcon
├──────────────────────────────────────┤
│  ┌────────────────────────────────┐  │
│  │      Hero Image (4:3)          │  │  Coil, ContentScale.Crop
│  └────────────────────────────────┘  │
│  $109.95                             │  titleLarge, bold, primary
│  Fjallraven - Foldsack No. 1...      │  titleMedium, max 3 lines
│  [men's clothing]  ★ 3.9 (120)       │  Chip + 可选 rating 行
│  ─────────────────────────────────   │
│  商品描述                             │  titleSmall
│  Your perfect pack for everyday...   │  bodyMedium, 全文滚动
│                                      │
├──────────────────────────────────────┤
│ [♡ 收藏]          [加入购物车]        │  底部 Bar / Row, 两 Button
└──────────────────────────────────────┘
```

| 元素 | 规范 |
|------|------|
| 根布局 | `Scaffold` + `TopAppBar` + `bottomBar` |
| 内容 | `Column` + `verticalScroll` |
| 收藏 | `OutlinedButton` + `Icons.Default.FavoriteBorder` / `Favorite` Toggle |
| 加购 | `Button` 全宽或 `weight(1f)`；点击 Snackbar「已加入购物车」 |
| 无 | 「立即购买」按钮 |

---

## 3. 分类 Tab

### 3a 分类列表

```
┌──────────────────────────────────────┐
│  TopAppBar: 「分类」                  │
├──────────────────────────────────────┤
│  [Grid icon]  electronics        >   │  ListItem + trailing chevron
│  [Diamond]   jewelery            >   │  LazyColumn
│  [Checkroom] men's clothing      >   │
│  [Woman]     women's clothing    >   │
└──────────────────────────────────────┘
```

**锁定**：**垂直列表**（非分类网格），每行 `ListItem` +  leading 分类图标 +  trailing `ChevronRight`。

### 3b 分类下商品

- TopAppBar 标题 = 分类名 + 返回
- 商品区 **复用首页同款 2 列 Card 网格**（共用 `ProductCard` composable）

---

## 4. 购物车 Tab

### Success 线框

```
┌──────────────────────────────────────┐
│  TopAppBar: 「购物车」                │
├──────────────────────────────────────┤
│  ┌──┬──────────────────────────────┐ │
│  │img│ Title max 2 lines            │ │  LazyColumn item
│  │   │ $109.95                      │ │
│  │   │ [－] 2 [＋]          [🗑]    │ │  IconButton 步进 + 删除
│  └──┴──────────────────────────────┘ │
│              …                       │
├──────────────────────────────────────┤
│  合计：$219.90                       │  bottomBar 区
│  [        去结算        ]            │  Button 全宽, enabled if 非空
└──────────────────────────────────────┘
```

| 元素 | 规范 |
|------|------|
| 行项 | `Row`：64dp 缩略图 + `Column`（标题、单价、步进器）+ 删除 `IconButton` |
| 步进器 | `IconButton(－)` + `Text(qty)` + `IconButton(＋)`；最小 1，删除用 🗑 或 `DeleteOutline` |
| 合计 | 实时 sum(price × qty) |
| 去结算 | 导航 `checkout`；购物车空时 `enabled = false` |

### Empty

```
      [ShoppingCartOutlined 64dp]
         购物车是空的
    [ 去首页逛逛 ]   ← TextButton → 切 Home Tab
```

---

## 5. 结算确认（push 全屏，implement 参考）

```
┌──────────────────────────────────────┐
│  [←]  确认订单                        │
├──────────────────────────────────────┤
│  商品摘要                             │
│  · Fjallraven... ×2      $219.90     │
│  · Mens Casual... ×1      $22.30     │
│  ─────────────────────────────────   │
│  合计：$242.20                       │
│  游客模式 · 无需支付                  │  labelMedium muted
├──────────────────────────────────────┤
│  [        确认下单        ]          │
└──────────────────────────────────────┘
```

---

## 6. 我的 Tab（订单列表，implement 参考）

```
┌──────────────────────────────────────┐
│  TopAppBar: 「我的」                  │
├──────────────────────────────────────┤
│  👤 游客模式                          │  ListItem 或 Banner
├──────────────────────────────────────┤
│  订单 #20260918-001                   │  Card clickable
│  2026-09-18  ·  已完成  ·  $242.20   │
│              …                       │
└──────────────────────────────────────┘
```

---

## 7. 共用组件（implement 清单）

| Composable | 用途 |
|------------|------|
| `ProductCard` | 首页 + 分类商品网格 |
| `ProductGrid` | `LazyVerticalGrid` + Paging `LazyPagingItems` |
| `LoadingContent` / `ErrorContent` / `EmptyContent` | 三态壳（可参数化文案） |
| `QuantityStepper` | 购物车行项 |
| `PriceText` | 统一 `$%.2f` 格式 |

---

## 8. 主题与 Token

MVP **不新建 design-system 仓库**；实现阶段：

- 沿用 `C2cmarketTheme` + **dynamicColor = true**（已有）
- 价格/CTA 可用 `MaterialTheme.colorScheme.primary`
- 错误重试：`FilledTonalButton`
- 间距节奏：**8dp 网格** — padding 16dp，卡片 gap 8dp，section 24dp

---

## 9. 无障碍要点

- 商品卡片：`contentDescription = "{title}, {price}"`
- 加购/收藏/重试/去结算：`contentDescription` 中文
- Loading 区：`liveRegion = Polite`（与天气 App 一致）
- 触摸目标 ≥ 48dp（步进器、删除）

---

## 10. 决策锁定表（#7 产出）

| 问题 | 决定 |
|------|------|
| 首页卡片 | 2 列网格：图 + 标题 + 价格 + 分类 Chip |
| 详情布局 | Hero 图 → 价格 → 标题 → 分类+rating → 描述；底栏加购+收藏 |
| 分类 Tab | 垂直 ListItem 列表 → 同款商品网格 |
| 购物车 | 行项图+标题+价+步进器+删除；底栏合计+去结算 |
| 详情导航 | push 全屏，隐藏 Bottom Nav |
| 主题 | Material 3 dynamicColor，不另建 design-system |
