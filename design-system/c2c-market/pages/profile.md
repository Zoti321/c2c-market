# Profile Page Overrides (v2+)

> **PROJECT:** C2C Market · **Platform:** Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Spec:** MVP 订单 + [v2.1 浏览历史](https://github.com/Zoti321/c2c-market/issues/21) + [v2.2 挂牌](https://github.com/Zoti321/c2c-market/issues/22) + [v2.3 地址](https://github.com/Zoti321/c2c-market/issues/23) + [v4.1 私信](https://github.com/Zoti321/c2c-market/issues/42) + [v4.2 卖出订单](https://github.com/Zoti321/c2c-market/issues/43) + [v4.4 收藏](https://github.com/Zoti321/c2c-market/issues/45)

## Layout（自上而下）

1. **v3.2 账号区**（`surfaceVariant` Card，替代纯游客 Banner）
   - **游客**：`PersonOutline` +「游客模式」+ 全宽 `OutlinedButton`「使用 Google 登录」
   - **已登录**：Coil 圆形头像 40dp + 显示名（headline）+ 邮箱（supporting, optional）+ `TextButton`「退出登录」
2. **v2.1** 最近浏览 LazyRow（有数据时）
3. **v4.4** ListItem「我的收藏」→ 收藏列表（`Icons.Outlined.FavoriteBorder` leading）
4. **v3.1 / v4.1** ListItem「我的消息」→ 买家会话列表（`Icons.Outlined.Chat` leading）
5. **v4.1** ListItem「收到的消息」→ 卖家收件箱（`Icons.Outlined.Chat` leading）
6. **v2.2** `FilledTonalButton`「发布挂牌」全宽，horizontal padding 16dp
7. **v2.2 / v4.2** 「我的挂牌」列表（有数据时；ListItem + 状态 Chip + 编辑/删除）
8. **v4.2** 「卖出订单」区块（含挂牌行项的订单；复用 OrderCard + 状态文案）
9. **v2.3** ListItem「收货地址」→ 地址列表
10. 「我的订单」区块（MVP 买入订单）

## 挂牌 ListItem（v4.2 扩展）

- leading：64dp 圆角图（Coil content URI）
- headline：标题
- supporting：价格（tertiary 色）
- **v4.2** 状态 Chip：`AVAILABLE` 在售 / `RESERVED` 已下单 / `SOLD` 已成交 / `REMOVED` 已下架
- **v4.2** `AVAILABLE`/`RESERVED` 时显示「标记已售」「下架」操作
- trailing：编辑 + 删除 IconButton

## 订单卡片（v4.2 / v4.3）

- 买入订单与卖出订单共用 OrderCard
- **v4.3** 状态文案：待确认 / 待面交 / 已完成 / 已取消（挂牌订单）；FakeStore 订单仍为已完成
