package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- ROOM ENTITIES ---

@Entity(tableName = "cart_items")
data class DbCartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val price: Double,
    val selectedSize: String,
    val colorName: String,
    val colorHex: Long,
    val garmentType: String,
    val quantity: Int
)

@Entity(tableName = "wishlist_items")
data class DbWishlistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val price: Double,
    val colorName: String,
    val colorHex: Long,
    val garmentType: String
)

@Entity(tableName = "past_orders")
data class DbPastOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderDate: Long,
    val totalAmount: Double,
    val itemsSummary: String, // Dynamic list of items: Hoodie, Tee, etc.
    val itemsCount: Int,
    val shippingName: String,
    val shippingAddress: String,
    val paymentCardLast4: String,
    val trackingStatus: String // "Processing", "Shipped", "Delivered"
)

// --- ROOM DAO ---

@Dao
interface LochaDao {

    // --- Cart Queries ---
    @Query("SELECT * FROM cart_items ORDER BY id DESC")
    fun getCartItems(): Flow<List<DbCartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: DbCartItem)

    @Update
    suspend fun updateCartItem(item: DbCartItem)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // --- Wishlist Queries ---
    @Query("SELECT * FROM wishlist_items ORDER BY id DESC")
    fun getWishlistItems(): Flow<List<DbWishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: DbWishlistItem)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun deleteWishlistItemByProduct(productId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId LIMIT 1)")
    suspend fun isProductInWishlist(productId: String): Boolean

    @Query("SELECT productId FROM wishlist_items")
    fun getWishlistedProductIds(): Flow<List<String>>

    // --- Past Orders Queries ---
    @Query("SELECT * FROM past_orders ORDER BY orderDate DESC")
    fun getPastOrders(): Flow<List<DbPastOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastOrder(order: DbPastOrder)
}

// --- APPDATABASE ---

@Database(
    entities = [DbCartItem::class, DbWishlistItem::class, DbPastOrder::class],
    version = 1,
    exportSchema = false
)
abstract class LochaDatabase : RoomDatabase() {
    abstract fun lochaDao(): LochaDao

    companion object {
        @Volatile
        private var INSTANCE: LochaDatabase? = null

        fun getDatabase(context: Context): LochaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LochaDatabase::class.java,
                    "locha_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- REPOSITORY IMPLEMENTATION ---

class LochaRepository(private val lochaDao: LochaDao) {

    val cartItems: Flow<List<DbCartItem>> = lochaDao.getCartItems()
    val wishlistItems: Flow<List<DbWishlistItem>> = lochaDao.getWishlistItems()
    val wishlistedProductIds: Flow<List<String>> = lochaDao.getWishlistedProductIds()
    val pastOrders: Flow<List<DbPastOrder>> = lochaDao.getPastOrders()

    suspend fun addToCart(
        productId: String,
        productName: String,
        price: Double,
        size: String,
        colorName: String,
        colorHex: Long,
        garmentType: String,
        qty: Int = 1
    ) {
        // Simple logic to add, or increment quantity if matching SKU (same product size color combo) is already in database
        // In mobile apps, it's nice to double check
        val newItem = DbCartItem(
            productId = productId,
            productName = productName,
            price = price,
            selectedSize = size,
            colorName = colorName,
            colorHex = colorHex,
            garmentType = garmentType,
            quantity = qty
        )
        lochaDao.insertCartItem(newItem)
    }

    suspend fun updateCartQuantity(cartItem: DbCartItem, newQty: Int) {
        if (newQty <= 0) {
            lochaDao.deleteCartItem(cartItem.id)
        } else {
            lochaDao.updateCartItem(cartItem.copy(quantity = newQty))
        }
    }

    suspend fun deleteCartItem(id: Int) = lochaDao.deleteCartItem(id)

    suspend fun clearCart() = lochaDao.clearCart()

    suspend fun toggleWishlist(product: Product, color: ProductColor) {
        val inWishlist = lochaDao.isProductInWishlist(product.id)
        if (inWishlist) {
            lochaDao.deleteWishlistItemByProduct(product.id)
        } else {
            lochaDao.insertWishlistItem(
                DbWishlistItem(
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    colorName = color.name,
                    colorHex = color.hexColor,
                    garmentType = product.garmentType.name
                )
            )
        }
    }

    suspend fun addToWishlist(product: Product, color: ProductColor) {
        if (!lochaDao.isProductInWishlist(product.id)) {
            lochaDao.insertWishlistItem(
                DbWishlistItem(
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    colorName = color.name,
                    colorHex = color.hexColor,
                    garmentType = product.garmentType.name
                )
            )
        }
    }

    suspend fun removeFromWishlist(productId: String) {
        lochaDao.deleteWishlistItemByProduct(productId)
    }

    suspend fun createPastOrder(
        items: List<DbCartItem>,
        totalAmount: Double,
        shippingName: String,
        shippingAddress: String,
        paymentCardLast4: String
    ) {
        val summary = items.joinToString(", ") { "${it.quantity}x ${it.productName} (${it.selectedSize}/${it.colorName})" }
        val itemsCount = items.sumOf { it.quantity }
        val pastOrder = DbPastOrder(
            orderDate = System.currentTimeMillis(),
            totalAmount = totalAmount,
            itemsSummary = summary,
            itemsCount = itemsCount,
            shippingName = shippingName,
            shippingAddress = shippingAddress,
            paymentCardLast4 = paymentCardLast4,
            trackingStatus = "Processing"
        )
        lochaDao.insertPastOrder(pastOrder)
        // Auto-clear cart after successful checkout
        lochaDao.clearCart()
    }
}
