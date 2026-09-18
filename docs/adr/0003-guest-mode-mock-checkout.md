# MVP 游客模式 + Mock 结算（无真支付）

MVP 聚焦买家浏览与下单闭环，不引入账号体系与支付 SDK，降低学习曲线。用户使用固定本地游客 ID；结算页确认后于 Room 生成 Mock 订单，不产生真实扣款。登录与 Google Sign-In 留待 v3。

**Status**: accepted

**Considered options**: 游客 + Mock 订单（选用）、Firebase Auth + 假支付（暂缓）、Stripe 测试模式（拒绝，MVP 范围外）
