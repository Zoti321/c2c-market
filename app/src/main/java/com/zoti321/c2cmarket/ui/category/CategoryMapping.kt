package com.zoti321.c2cmarket.ui.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.Category

data class CategoryItem(
    val slug: Category,
    val displayName: String,
    val icon: ImageVector,
)

@Composable
fun mapCategory(slug: String): CategoryItem {
    val displayName = when (slug) {
        "electronics" -> stringResource(R.string.category_electronics)
        "jewelery" -> stringResource(R.string.category_jewelery)
        "men's clothing" -> stringResource(R.string.category_mens_clothing)
        "women's clothing" -> stringResource(R.string.category_womens_clothing)
        else -> slug
    }
    val icon = when (slug) {
        "electronics" -> Icons.Outlined.Devices
        "jewelery" -> Icons.Outlined.Diamond
        "men's clothing" -> Icons.Outlined.Checkroom
        "women's clothing" -> Icons.Outlined.ShoppingBag
        else -> Icons.Outlined.Category
    }
    return CategoryItem(slug = slug, displayName = displayName, icon = icon)
}

@Composable
fun mapCategories(slugs: List<String>): List<CategoryItem> =
    slugs.map { mapCategory(it) }

@Composable
fun categoryDisplayName(slug: String): String = mapCategory(slug).displayName
