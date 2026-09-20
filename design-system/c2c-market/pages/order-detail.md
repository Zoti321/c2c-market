# Order Detail Page Overrides (v3.3)

> **PROJECT:** C2C Market · **Platform:** Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)  
> **Spec:** [约定 v3.3 Deep Link 与地图规格](https://github.com/Zoti321/c2c-market/issues/32)

## v3.3 地图入口

- 收货地址区块下方：`TextButton`「在地图中查看」
- **显示条件**：订单含 `shipping` 快照且地址非空
- 点击 → 系统 `geo:` Intent（不调 Maps SDK）
