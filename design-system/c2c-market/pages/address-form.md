# Address Form Page Overrides

> **Spec:** [约定 v2.3 收货地址 CRUD 与结算集成规格](https://github.com/Zoti321/c2c-market/issues/23)

## Layout

- `Scaffold` + TopAppBar「新建地址」/「编辑地址」
- 单列 `OutlinedTextField` × 4 + `Switch`「设为默认地址」
- 底部 Primary「保存」（tertiary 绿）

## Fields order

1. 收货人
2. 手机号（`KeyboardType.Phone`）
3. 省市区
4. 详细地址（`minLines = 2`）

## Validation

- 内联 `SupportingText` error；提交时统一校验
