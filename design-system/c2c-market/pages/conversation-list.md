# Conversation List Page Overrides (v3.1)

> **PROJECT:** C2C Market · **Platform:** Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Spec:** [约定 v3.1 私信与聊天规格](https://github.com/Zoti321/c2c-market/issues/30)

## Layout

- 从我的 Tab `ListItem`「我的消息」push 全屏，**隐藏 Bottom Nav**
- `TopAppBar` 标题「我的消息」+ `NavigationIcon` 返回
- `LazyColumn` 会话行；无 FAB

## List Item

- leading：48dp 圆角商品缩略图（Coil；挂牌用 content URI）
- headline：商品标题（单行 ellipsis）
- supporting：`卖家 · {sellerDisplayName}` + 最后消息预览（单行 ellipsis）
- trailing：相对时间（`lastMessageAt`）+ 未读 Badge（`unreadCount > 0` 时）
- 点击 → `ChatScreen`

## States

- Loading：`CircularProgressIndicator` 居中
- Empty：「暂无消息」+ 副文案「在商品详情点击「联系卖家」开始对话」
- Error：共用 `ErrorContent` + 重试
