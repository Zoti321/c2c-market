# Address List Page Overrides

> **Spec:** [约定 v2.3 收货地址 CRUD 与结算集成规格](https://github.com/Zoti321/c2c-market/issues/23)

## Layout

- `Scaffold` + `TopAppBar`「收货地址」+ 返回 + FAB/add action
- `LazyColumn` of `ListItem`
- 默认地址：`AssistChip`「默认」+ 置顶

## ListItem

- headline：`receiverName` + `phone`（同一行或 supporting 分行）
- supporting：`region + detail` 最多两行
- trailing：非默认项「设为默认」；删除 IconButton

## States

- Empty：`LocationOff` + 「暂无收货地址」+ Primary「添加地址」

## Navigation

- Profile「收货地址」入口
- Checkout「管理地址」
