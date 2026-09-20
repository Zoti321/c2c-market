# Product Detail Page Overrides

> **PROJECT:** C2C Market · **Platform:** Android Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)

## Layout

- Push 全屏，隐藏 Bottom Nav
- `TopAppBar` + `NavigationIcon` 返回
- `Column(verticalScroll)` + `bottomBar` 固定操作区

## Visual Hierarchy

1. Hero 图 4:3，`ContentScale.Crop`
2. **价格** — `titleLarge` bold，`#16A34A`（accent）
3. 标题 — `titleMedium`，`#0F172A`
4. 分类 Chip + rating 行 — `bodySmall`，muted `#475569`（**v2.2 挂牌**：显示「本地挂牌」Chip，**隐藏** rating）
5. 描述 — `bodyMedium`，段落间距 8dp

## Actions

### v3.1 联系卖家（描述区下方）

- `OutlinedButton` 全宽「联系卖家」；horizontal padding 与正文一致
- **隐藏条件**：`ProductSource.LOCAL_LISTING` 且为当前设备**我的挂牌**（v3.1 单游客同机）
- 点击 → getOrCreate 会话 → push `ChatScreen`

### bottomBar

| 按钮 | 样式 | 色 |
|------|------|-----|
| 收藏 | `OutlinedButton` | primary `#2563EB` border |
| 加入购物车 | `Button` | accent `#16A34A` fill |

- Snackbar 反馈：「已加入购物车」
- 触摸目标 ≥ 48dp

## States

- 图片 Loading：Coil placeholder 灰色 surface
- Error：同首页 Error 模式
