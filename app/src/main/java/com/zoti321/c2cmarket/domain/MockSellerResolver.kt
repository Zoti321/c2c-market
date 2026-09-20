package com.zoti321.c2cmarket.domain

import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ProductSource

object MockSellerResolver {

    data class MockSeller(
        val sellerId: String,
        val sellerDisplayName: String,
    )

    fun resolve(product: Product): MockSeller? {
        if (product.source == ProductSource.LOCAL_LISTING) return null
        return MockSeller(
            sellerId = "mock-seller-${product.id}",
            sellerDisplayName = "卖家 · ${categoryLabel(product.category)}",
        )
    }

    private fun categoryLabel(category: String): String = when (category) {
        "electronics" -> "电子产品"
        "jewelery" -> "珠宝首饰"
        "men's clothing" -> "男装"
        "women's clothing" -> "女装"
        else -> category
    }
}
