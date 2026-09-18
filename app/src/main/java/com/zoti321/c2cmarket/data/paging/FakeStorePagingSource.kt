package com.zoti321.c2cmarket.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.remote.FakeStoreApi
import com.zoti321.c2cmarket.domain.model.Product

class FakeStorePagingSource(
    private val api: FakeStoreApi,
    private val sort: String = "desc",
    private val pageSize: Int = 10,
) : PagingSource<Int, Product>() {

    private var cached: List<Product>? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Product> {
        return try {
            val page = params.key ?: 0
            val all = cached ?: api.getProducts(sort = sort).map { it.toDomain() }
                .also { cached = it }

            val from = page * pageSize
            if (from >= all.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = page - 1,
                    nextKey = null,
                )
            }
            val to = minOf(from + pageSize, all.size)
            val slice = all.subList(from, to)
            val nextKey = if (to < all.size) page + 1 else null

            LoadResult.Page(
                data = slice,
                prevKey = if (page == 0) null else page - 1,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Product>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
}
