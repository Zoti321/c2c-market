# Chat Page Overrides (v3.1)

> **PROJECT:** C2C Market · **Platform:** Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Spec:** [约定 v3.1 私信与聊天规格](https://github.com/Zoti321/c2c-market/issues/30)

## Layout

- Push 全屏，隐藏 Bottom Nav
- `TopAppBar`：商品标题（subtitle 显示卖家名）+ 返回
- 消息区：`LazyColumn` 反向或正向 + 自动滚到底
- 底部：输入区固定 `Surface`（ tonalElevation 3dp）

## Message Bubble

| 发送方 | 对齐 | 背景 |
|--------|------|------|
| 当前用户（买家） | 右 | `primaryContainer` |
| 卖家 | 左 | `surfaceVariant` |

- 正文 `bodyMedium`；时间戳 `labelSmall` muted，气泡下方
- 草稿不在列表展示（仅输入框）

## Input Area

- `OutlinedTextField` 单行 expandable（max 500 字符）+ `IconButton` 发送
- 发送按钮：`Icons.AutoMirrored.Filled.Send`；空文本 disabled
- 输入变化 → debounce 300ms 保存草稿

## States

- Loading 历史：消息区 skeleton 或居中 indicator
- 发送中：发送按钮 disabled
- 发送失败：Snackbar「发送失败，请重试」；草稿保留
