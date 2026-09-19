# Cart Page Overrides

> **PROJECT:** C2C Market · **Platform:** Android Jetpack Compose  
> **Overrides:** [`MASTER.md`](../MASTER.md)

## Layout

- `LazyColumn` 行项 + `bottomBar`  sticky 合计区（safe area inset）
- 行项间距 12dp；section padding 16dp

## Cart Line Item

- 64dp 缩略图（圆角 8dp）+ 标题/单价 + `QuantityStepper` + `DeleteOutline`（destructive `#DC2626`）
- 步进器 IconButton min 48dp

## Bottom Bar

```
合计：$xxx.xx   (titleMedium, foreground)
[ 去结算 ]      (Button fullWidth, accent #16A34A)
```

- 购物车空：`ShoppingCartOutlined` + 文案 + `TextButton`「去首页逛逛」

## Checkout CTA

- 主 CTA 使用 accent green，与 MASTER「Conversion CTA」一致
- disabled 态：购物车空时 alpha 0.38
