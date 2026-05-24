package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    POPULARITY, PRICE_LOW_TO_HIGH, PRICE_HIGH_TO_LOW
}

class LochaViewModel(
    private val repository: LochaRepository,
    private val supabaseRepository: SupabaseRepository = SupabaseRepository()
) : ViewModel() {

    // --- SEARCH / CATEGORY STATES ---
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.POPULARITY)
    val sortOption = _sortOption.asStateFlow()

    // --- DB CONNECTED FLOWS ---
    val cartItems: StateFlow<List<DbCartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<DbWishlistItem>> = repository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistedProductIds: StateFlow<Set<String>> = repository.wishlistedProductIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val pastOrders: StateFlow<List<DbPastOrder>> = repository.pastOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _remoteProducts = MutableStateFlow<List<FullProduct>>(emptyList())

    init {
        fetchSupabaseProducts()
    }

    private fun fetchSupabaseProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val products = supabaseRepository.fetchProducts()
                _remoteProducts.value = products
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to local catalog if network fails
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- DYNAMIC CATALOG FLOWS ---
    val filteredProducts: StateFlow<List<Product>> = combine(
        _remoteProducts, _selectedCategory, _searchQuery, _sortOption
    ) { remote, category, query, sort ->
        // Map Supabase FullProduct to legacy UI Product model
        val mappedList = if (remote.isNotEmpty()) {
            remote.map { fp ->
                Product(
                    id = fp.product.id,
                    name = fp.product.name,
                    category = fp.category?.name ?: "All",
                    price = fp.product.basePrice,
                    description = fp.product.description ?: "",
                    availableColors = ProductCatalog.products.firstOrNull()?.availableColors ?: emptyList(),
                    defaultColor = ProductCatalog.products.firstOrNull()?.defaultColor ?: ProductCatalog.COLORS_OBSIDIAN,
                    garmentType = GarmentType.TEE, // Default placeholder
                    rating = 5.0f,
                    reviewCount = 0,
                    isNewDrop = fp.product.isNewDrop
                )
            }
        } else {
            ProductCatalog.products
        }

        var list = mappedList

        // Category Filter
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Search Query Filter
        if (query.isNotEmpty()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        // Sorting
        when (sort) {
            SortOption.POPULARITY -> list.sortedByDescending { it.rating }
            SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.price }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CART CALCULATIONS ---
    val subtotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val shippingFee: StateFlow<Double> = subtotal.map { sub ->
        if (sub > 150.0 || sub == 0.0) 0.0 else 12.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val orderTotal: StateFlow<Double> = combine(subtotal, shippingFee) { sub, ship ->
        sub + ship
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- CHECKOUT FORM INPUTS (Kept in VM to survive screen flips) ---
    val checkoutStep = MutableStateFlow(1) // 1 = Address, 2 = Payment, 3 = Confirmation
    val shippingName = MutableStateFlow("")
    val shippingAddress = MutableStateFlow("")
    val shippingCity = MutableStateFlow("")
    val shippingZip = MutableStateFlow("")
    val cardNumber = MutableStateFlow("")
    val cardExpiry = MutableStateFlow("")
    val cardCvv = MutableStateFlow("")
    
    // --- ACTIONS ---
    
    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            repository.toggleWishlist(product, product.defaultColor)
        }
    }

    fun addToCart(product: Product, size: String, color: ProductColor, qty: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(
                productId = product.id,
                productName = product.name,
                price = product.price,
                size = size,
                colorName = color.name,
                colorHex = color.hexColor,
                garmentType = product.garmentType.name,
                qty = qty
            )
        }
    }

    fun updateCartQty(cartItem: DbCartItem, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItem, newQty)
        }
    }

    fun removeCartItem(id: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(id)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun processOrderCheckout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val items = cartItems.value
            val total = orderTotal.value
            if (items.isNotEmpty()) {
                val last4 = if (cardNumber.value.length >= 4) cardNumber.value.takeLast(4) else "4321"
                repository.createPastOrder(
                    items = items,
                    totalAmount = total,
                    shippingName = shippingName.value,
                    shippingAddress = "${shippingAddress.value}, ${shippingCity.value} ${shippingZip.value}",
                    paymentCardLast4 = last4
                )
                checkoutStep.value = 3
                onSuccess()
            }
        }
    }

    fun resetCheckout() {
        checkoutStep.value = 1
        shippingName.value = ""
        shippingAddress.value = ""
        shippingCity.value = ""
        shippingZip.value = ""
        cardNumber.value = ""
        cardExpiry.value = ""
        cardCvv.value = ""
    }

    // --- VIEWMODEL FACTORY PROTOCOL ---
    class Factory(private val repository: LochaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LochaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LochaViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
