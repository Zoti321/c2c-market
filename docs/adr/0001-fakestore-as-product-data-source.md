# 采用 FakeStore API 作为商品远程数据源

学习项目需要免费、免 API Key、JSON 结构清晰的公开 API 来练习 Retrofit 与 Paging。FakeStore 提供商品列表、详情与分类接口，适合模拟电商浏览场景。其无真实 C2C 语义（无卖家、无私信），挂牌与聊天将在 v2/v3 以本地 Mock 补充。

**Status**: accepted

**Considered options**: FakeStore（选用）、自建 Mock Server（暂缓，复杂度高）、JSONPlaceholder（拒绝，非电商语义）
