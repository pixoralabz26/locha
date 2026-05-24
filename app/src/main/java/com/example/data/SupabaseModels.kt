package com.example.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseCategory(
    val id: String,
    val name: String,
    val slug: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SupabaseProduct(
    val id: String,
    @SerialName("category_id") val categoryId: String? = null,
    val name: String,
    val slug: String,
    val description: String? = null,
    @SerialName("base_price") val basePrice: Double,
    @SerialName("sale_price") val salePrice: Double? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("is_new_drop") val isNewDrop: Boolean = false,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SupabaseProductVariant(
    val id: String,
    @SerialName("product_id") val productId: String,
    val size: String? = null,
    val color: String? = null,
    @SerialName("stock_qty") val stockQty: Int = 0,
    @SerialName("price_override") val priceOverride: Double? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SupabaseProductImage(
    val id: String,
    @SerialName("product_id") val productId: String,
    val url: String,
    val blurhash: String? = null,
    @SerialName("display_order") val displayOrder: Int = 0
)

// A composite model representing a Product fully joined with its Variants and Images
data class FullProduct(
    val product: SupabaseProduct,
    val category: SupabaseCategory? = null,
    val variants: List<SupabaseProductVariant> = emptyList(),
    val images: List<SupabaseProductImage> = emptyList()
)
