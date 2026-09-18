# FakeStore API 调研报告

> **关联 Issue**：[调研 FakeStore API 能力与集成要点](https://github.com/Zoti321/c2c-market/issues/3)  
> **调研日期**：2026-09-18  
> **数据来源**：[FakeStore 官方 README](https://github.com/keikaavousi/fake-store-api/blob/master/README.md)、[fakestoreapi.com/docs](https://fakestoreapi.com/docs)（OpenAPI v2.1.11）、2026-09-18 实测 HTTPS 响应

## 结论摘要

FakeStore **满足 MVP 商品浏览需求**：免费、免 API Key、HTTPS JSON，提供商品列表/详情/分类接口，字段含 `title`、`price`、`image`、`category`、`description`、`rating`。  
**限制**：全库仅 **20 条**商品；**无官方 offset/page 分页**（README ToDo 仍列「Add pagination」）；仅支持 `limit` 与 `sort`。Android 端推荐 **一次拉全量 + 客户端 Paging 3 切片**，或首页直接 `GET /products` 不分页。无效 ID 返回 **HTTP 200 + 空 body**，Repository 须自行判空转 Error。

---

## 1. Base URL 与认证

| 项 | 值 |
|----|-----|
| Base URL | `https://fakestoreapi.com/` |
| 认证 | 无（MVP 只读 GET 即可） |
| Content-Type | `application/json` |

---

## 2. MVP 相关 Endpoints

### 2.1 商品列表

```
GET https://fakestoreapi.com/products
GET https://fakestoreapi.com/products?limit={n}
GET https://fakestoreapi.com/products?sort=asc|desc
GET https://fakestoreapi.com/products?limit={n}&sort=desc
```

**实测（2026-09-18）**：

- 无参数：返回 **20** 条，id 1–20
- `limit=3`：返回 3 条 `[1,2,3]`
- `sort=desc` + `limit=3`：返回 `[3,2,1]`（按 id 排序，非价格）
- `offset=5`：**不被支持**（仍从 id 1 起返回，与官方文档一致——无 offset 参数）

**单条 Product JSON 示例**：

```json
{
  "id": 1,
  "title": "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
  "price": 109.95,
  "description": "Your perfect pack for everyday use...",
  "category": "men's clothing",
  "image": "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_t.png",
  "rating": { "rate": 3.9, "count": 120 }
}
```

| 字段 | 类型 | MVP 用途 |
|------|------|----------|
| `id` | Number | 主键、导航、Room 外键 |
| `title` | String | 列表/详情标题 |
| `price` | Number | 价格展示、购物车合计 |
| `description` | String | 详情页（部分条目较短） |
| `category` | String | 分类 Tab 筛选 |
| `image` | String | Coil HTTPS URL，域名 `fakestoreapi.com` |
| `rating.rate` | Number | 可选展示 |
| `rating.count` | Number | 可选展示 |

### 2.2 商品详情

```
GET https://fakestoreapi.com/products/{id}
```

**实测**：

- `id=1`：200 + 完整对象
- `id=99999` / `id=0` / `id=abc`：**200 + 空 body**（非 404 JSON）

→ Repository 应：`response.body()` 为空或缺 `id` 时抛出/返回 **NotFound**。

### 2.3 分类列表

```
GET https://fakestoreapi.com/products/categories
```

**实测响应**：

```json
["electronics", "jewelery", "men's clothing", "women's clothing"]
```

> 官方 README 另写 `/products/products/categories`，实测 **`/products/categories`** 可用，以实测为准。

### 2.4 按分类筛选

```
GET https://fakestoreapi.com/products/category/{categoryName}
GET https://fakestoreapi.com/products/category/{categoryName}?limit={n}&sort=desc
```

**实测**：

- `category/electronics`：返回该分类商品数组
- `category/nonexistent`：**200 + `[]`**（空数组，非错误码）

分类名须 URL 编码（如 `men's clothing` → `men%27s%20clothing`）。

---

## 3. 分页与 Paging 3 集成

| 能力 | FakeStore | 建议 |
|------|-----------|------|
| 服务端 page/offset | ❌ 不支持 | 不依赖 `offset` / `page` |
| `limit` | ✅ 仅限制返回条数 | 可用于单页大小，不能翻页 |
| `sort` | ✅ asc / desc（默认 asc） | 首页可选 desc 展示 |
| 全量条数 | 20 | 体量小，可一次缓存 |

**推荐 MVP 方案（锁定供 #12 引用）**：

1. **`FakeStorePagingSource`**：首次 `GET /products` 拉全量 → 内存列表按 `pageSize`（如 10）切片 → `LoadResult.Page`；`nextKey = null` 当无更多数据。
2. **分类页**：`GET /products/category/{name}` 全量（通常 ≤ 6 条）→ 同样客户端切片或不分页。
3. **备选（更简单）**：首页不用 Paging，直接 `Flow<List<Product>>`；仍可在 #12 决定保留 Paging 练习价值。

**不推荐**：RemoteMediator + Room 商品缓存（MVP 过度）；伪造 offset 请求（API 不支持）。

---

## 4. 错误、限流与网络

| 场景 | 行为 | App 处理 |
|------|------|----------|
| 无网络 | Retrofit/OkHttp 抛异常 | UI Error + 重试 |
| 无效 product id | 200 + 空 body | Repository → NotFound |
| 无效 category | 200 + `[]` | UI Empty |
| HTTP 4xx/5xx | 少见（读操作稳定） | 统一 Error 文案 |
| 限流 | 官方未文档化；实测无 Rate-Limit 头 | MVP 无需 backoff；注意勿循环狂刷 |

POST/PUT/DELETE 返回假数据且不持久化——**MVP 不使用**（购物车/订单走 Room）。

---

## 5. 建议 DTO（kotlinx.serialization）

```kotlin
@Serializable
data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: RatingDto = RatingDto(),
)

@Serializable
data class RatingDto(
    val rate: Double = 0.0,
    val count: Int = 0,
)
```

Domain 模型 `Product` 与 DTO 分离；`ProductDto.toDomain()` 在 data 层完成。

---

## 6. 建议 Retrofit 接口

```kotlin
interface FakeStoreApi {
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: String? = null,
    ): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: String? = null,
    ): List<ProductDto>
}
```

---

## 7. 建议 Repository 接口（草案）

```kotlin
interface ProductRepository {
    fun pagingProducts(pageSize: Int = 10): Flow<PagingData<Product>>
    suspend fun getProduct(id: Int): Result<Product>
    suspend fun getCategories(): Result<List<String>>
    suspend fun getProductsByCategory(category: String): Result<List<Product>>
}
```

实现类 `FakeStoreProductRepository`：`@Singleton`，注入 `FakeStoreApi`；详情接口须校验空 body。

---

## 8. 与 C2C MVP 的语义差距

| FakeStore 有 | C2C MVP 需要 | 处理 |
|-------------|-------------|------|
| 固定 20 商品 | 商品浏览 | ✅ 够用 |
| 无卖家 | C2C 卖家 | v2.2 本地挂牌 Mock |
| 无库存 | 库存 | MVP 忽略或 Mock 无限 |
| `/carts` API | 购物车 | ❌ 不用；Room SSOT（ADR-0002） |

---

## 9. 参考链接

- [FakeStore GitHub README](https://github.com/keikaavousi/fake-store-api/blob/master/README.md)
- [FakeStore OpenAPI Docs](https://fakestoreapi.com/docs)
- [ADR-0001：FakeStore 数据源](../adr/0001-fakestore-as-product-data-source.md)
