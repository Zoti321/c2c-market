# Home Page Overrides

> **PROJECT:** C2C Market · **Platform:** Android Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Wireframe:** [`docs/design/c2c-mvp-ui-prototype.md`](../../../docs/design/c2c-mvp-ui-prototype.md)

## MVP / v2.1 适配说明

- **v2.1 起**：TopAppBar **actions** 增加搜索图标 → push [`search.md`](search.md) 全屏搜索页
- 首屏仍为 **2 列商品网格**（非 Hero 搜索为主 CTA）
- 增加 **Trust 条**：列表底部 `labelSmall`「商品数据来自 FakeStore」

## Layout

- `Scaffold` + `TopAppBar`「首页」`CenterAligned` + **Search action**（v2.1）
- **v2.2**：有本地挂牌时，网格 **前若干项** 为挂牌 `ProductCard`（带「本地挂牌」小 Chip），其余为 FakeStore Paging
- `LazyVerticalGrid` `GridCells.Fixed(2)`，contentPadding 16dp，gap 8dp
- 无 Hero、TopAppBar 内嵌 SearchBar（搜索在独立页）

## Components

- **ProductCard**：白底 Card，圆角 12dp，elevation 1dp（Flat，非 web hover shadow）
- 价格色：`#16A34A`（accent / price green）
- 分类：`AssistChip`，primary 容器色淡化

## States

- Loading：网格区居中 `CircularProgressIndicator`（primary `#2563EB`）
- Error：`CloudOff` + 文案 + `FilledTonalButton`「重试」
- Empty：`Inventory2` + 「暂无商品」
