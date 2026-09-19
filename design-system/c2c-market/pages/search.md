# Search Page Overrides

> **PROJECT:** C2C Market · **Platform:** Android Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Spec:** [约定 v2.1 搜索与浏览历史规格](https://github.com/Zoti321/c2c-market/issues/21)

## Layout

- `Scaffold` + `TopAppBar`「搜索」+ 返回
- 顶部 `SearchBar`（M3，`active = true`），占满宽，padding 16dp
- 下方 `LazyColumn` 单列 `ProductCard`（同首页卡片样式）
- 无 Bottom Nav

## States

- **Idle**（无输入）：居中 `bodyMedium` 引导文案「输入商品名称搜索」
- **Loading**：列表区居中 `CircularProgressIndicator`
- **Empty**：`SearchOff` icon + 「未找到相关商品」
- **Error**：共用 `ErrorContent` + 重试

## Navigation

- 入口：首页 TopAppBar `Search` action
- 结果点击 → `product/{id}` push
