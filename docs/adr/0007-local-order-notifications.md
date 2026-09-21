# ADR-0007：Mock 订单本地通知（WorkManager）

## 背景

v2.4 需练习 WorkManager 与 NotificationChannel。C2C Mock 结算无真实物流，需在下单后用延迟本地通知模拟配送提醒。

## 决策

- 使用 **OneTimeWorkRequest**（15s 延迟），由 `OrderNotificationScheduler` 入队。
- **仅本地通知**；不引入 FCM。
- 通知**不改变订单状态**；点击通知通过 Deep Link 打开订单详情。
- 无通知权限时跳过调度，不阻塞下单。

### v4.3 通知种类（挂牌订单）

| 种类 | 触发时机 | 受众 |
|------|----------|------|
| `PENDING_SELLER` | 含挂牌订单创建 | 卖家 |
| `CONFIRMED_BUYER` | 卖家确认订单 | 买家 |
| `COMPLETED` | 双方面交确认完成 | 双方 |
| `SHIPPED` | 纯 FakeStore 订单下单（v2.4 沿用） | 买家 |

## 后果

- 新增 `work-runtime-ktx`、`hilt-work` 依赖与 `@HiltWorker` 配置。
- `POST_NOTIFICATIONS` 需在 API 33+ 运行时申请。
