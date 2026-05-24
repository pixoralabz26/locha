package com.example.data

import com.example.network.Supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository {

    private val client = Supabase.client

    suspend fun fetchCategories(): List<SupabaseCategory> = withContext(Dispatchers.IO) {
        client.postgrest["categories"]
            .select()
            .decodeList<SupabaseCategory>()
    }

    suspend fun fetchProducts(): List<FullProduct> = withContext(Dispatchers.IO) {
        // In a real app with large data, we would use Supabase joins (e.g. `select="*, product_variants(*), product_images(*)"`)
        // and decode directly. Since Supabase Postgrest-kt supports relational queries, we can fetch them together, 
        // or fetch sequentially for simplicity.
        
        val products = client.postgrest["products"]
            .select()
            .decodeList<SupabaseProduct>()
            
        val variants = client.postgrest["product_variants"]
            .select()
            .decodeList<SupabaseProductVariant>()
            
        val images = client.postgrest["product_images"]
            .select()
            .decodeList<SupabaseProductImage>()
            
        val categories = fetchCategories()

        // Assemble them in-memory
        products.map { product ->
            FullProduct(
                product = product,
                category = categories.find { it.id == product.categoryId },
                variants = variants.filter { it.productId == product.id },
                images = images.filter { it.productId == product.id }.sortedBy { it.displayOrder }
            )
        }
    }
}
