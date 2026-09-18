# Room 作为购物车、收藏、订单的本地 SSOT

购物车、收藏与订单是买家闭环的核心状态，须离线可用且进程重启后保留。Room 提供类型安全的 DAO、Flow 观察与事务，比 SharedPreferences / 裸 JSON 更适合关系型数据。FakeStore 仅作商品远程源；本地写入（加购、收藏、下单）以 Room 为唯一真相源，Repository 负责协调远程与本地。

**Status**: accepted

**Considered options**: Room（选用）、DataStore only（拒绝，不适合购物车/订单结构）、内存 + 无持久化（拒绝，不符合 MVP 验收）
