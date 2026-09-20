# Profile Page Overrides (v2+)

> **PROJECT:** C2C Market · **Platform:** Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Spec:** MVP 订单 + [v2.1 浏览历史](https://github.com/Zoti321/c2c-market/issues/21) + [v2.2 挂牌](https://github.com/Zoti321/c2c-market/issues/22) + [v2.3 地址](https://github.com/Zoti321/c2c-market/issues/23)

## Layout（自上而下）

1. 游客 Banner（`surfaceVariant` Card）
2. **v2.1** 最近浏览 LazyRow（有数据时）
3. **v2.2** `FilledTonalButton`「发布挂牌」全宽，horizontal padding 16dp
4. **v2.2** 「我的挂牌」列表（有数据时；ListItem + 删除）
5. **v2.3** ListItem「收货地址」→ 地址列表
6. 「我的订单」区块（MVP）

## 挂牌 ListItem

- leading：64dp 圆角图（Coil content URI）
- headline：标题
- supporting：价格（tertiary 色）
- trailing：删除 IconButton
