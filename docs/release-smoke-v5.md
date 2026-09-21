# v5 Release Smoke（L3 手工双设备）

PR 合入 `main` 前勾选。CI 不跑本清单；**阻塞 merge**（人工 gate）。

## F0 前置

- [ ] Firebase 项目已创建（Spark 即可）
- [ ] `app/google-services.json` 已从 example 复制并填入真实值
- [ ] `local.properties` 已配置 `google.web_client_id`
- [ ] 两台设备（或 2 模拟器）+ 2 个 Google 测试账号
- [ ] 通知权限已授予（Android 13+）

可选：`firebase deploy --only functions,firestore:rules,storage`

## F1 聊天跨设备（#52）

- [ ] 设备 A（买家）发消息 → 设备 B（卖家，同 Google 账号）实时收到
- [ ] A 离线发消息 → 联网后 B 收到
- [ ] B 打开会话 markRead → A 端 unread 清零
- [ ] A 输入草稿未发送 → B 不可见
- [ ] FakeStore mock-seller 会话：本地 auto-reply，Firestore 无 doc

## F2 挂牌图片（#53）

- [ ] 卖家 A 新建挂牌选图 → Room HTTPS；设备 B 同账号见同图
- [ ] 卖家 A 更新图片 → Storage 覆盖；B 刷新见新图
- [ ] v4 `content://` 挂牌 → 登录懒迁移或编辑时 upload 成功
- [ ] 买家 B 浏览 A 挂牌 → Coil 加载 HTTPS 正常
- [ ] 删除挂牌 → Room 删；Storage 对象清理

## F3 FCM Push（#54）

- [ ] A 发消息，B 后台 → B 收 chat 通知，点击进会话
- [ ] A 发消息，B 前台 → 无通知，UI 实时更新
- [ ] 卖家确认面交订单，买家后台 → order 通知，点击进订单详情
- [ ] FakeStore 非面交下单 → 15s 后 WorkManager SHIPPED（非 FCM）
- [ ] 登出 → FCM token 从 Firestore 删除

## F4 订单/挂牌 sync（#55）

- [ ] 面交下单 → 挂牌 RESERVED；卖家/买家双端见订单
- [ ] 卖家确认 → CONFIRMED sync
- [ ] 双方面交确认 → COMPLETED + 挂牌 SOLD
- [ ] 取消订单 → 挂牌回 AVAILABLE
- [ ] FakeStore 纯订单：local-only，无 Firestore doc

## F5 v4 回归

- [ ] FakeStore 浏览 / 搜索 / 分类 / 购物车 / 收藏
- [ ] Deep Link `c2cmarket://app/product/{id}` / `order/{id}` / `chat/{id}`
- [ ] 冷启动 Room migration v10→v11 无崩溃
- [ ] 面交确认 / 收藏列表 / 用户隔离关键路径
