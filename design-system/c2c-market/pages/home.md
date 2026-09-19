# Home Page Overrides

> **PROJECT:** C2C Market · **Platform:** Android Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Wireframe:** [`docs/design/c2c-mvp-ui-prototype.md`](../../../docs/design/c2c-mvp-ui-prototype.md)

## MVP 适配说明

- MASTER 默认「搜索为主 CTA」—— **MVP 不做搜索**（见 mvp-scope）；首页首屏即 **2 列商品网格**
- 增加 **Trust 条**：列表底部 `labelSmall`「商品数据来自 FakeStore」（反模式：No trust cues）

## Layout

- `Scaffold` + `TopAppBar`「首页」`CenterAligned`
- `LazyVerticalGrid` `GridCells.Fixed(2)`，contentPadding 16dp，gap 8dp
- 无 Hero、无搜索栏

## Components

- **ProductCard**：白底 Card，圆角 12dp，elevation 1dp（Flat，非 web hover shadow）
- 价格色：`#16A34A`（accent / price green）
- 分类：`AssistChip`，primary 容器色淡化

## States

- Loading：网格区居中 `CircularProgressIndicator`（primary `#2563EB`）
- Error：`CloudOff` + 文案 + `FilledTonalButton`「重试」
- Empty：`Inventory2` + 「暂无商品」
