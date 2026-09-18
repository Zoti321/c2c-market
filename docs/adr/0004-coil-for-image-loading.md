# 采用 Coil 加载商品图片

商品列表与详情需异步加载 HTTPS 图片。Coil 与 Compose 集成良好、API 简洁、支持 OkHttp 共享，适合 Jetpack Compose 项目。Glide 功能成熟但 Compose 集成较重；手写 Bitmap 加载不符合架构目标。

**Status**: accepted

**Considered options**: Coil（选用）、Glide（暂缓）、Compose AsyncImage 裸 URL（拒绝，缺少缓存与生命周期）
