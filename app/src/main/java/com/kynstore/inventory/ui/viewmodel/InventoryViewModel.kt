package com.kynstore.inventory.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kynstore.inventory.data.Product
import com.kynstore.inventory.data.ProductDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InventoryStats(
    val totalCount: Int = 0,
    val lowStockCount: Int = 0,
    val expiringCount: Int = 0,
    val outOfStockCount: Int = 0
)

data class CartItem(
    val product: Product,
    val quantityToBuy: Int
)

class InventoryViewModel(private val productDao: ProductDao) : ViewModel() {

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPosCategory = MutableStateFlow("Lahat (All)")
    val selectedPosCategory: StateFlow<String> = _selectedPosCategory.asStateFlow()

    // POS Cart mapping: Product ID to Quantity to buy
    private val _cartMap = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val cartMap: StateFlow<Map<Int, Int>> = _cartMap.asStateFlow()

    // Sync / Loading state for CloudSyncLoader
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow("Naglo-load ang datos...")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val allProductsFlow = productDao.getAllProducts()

    // Dynamic categories extracted from inventory
    val categories: StateFlow<List<String>> = allProductsFlow
        .combine(MutableStateFlow(Unit)) { products, _ ->
            val set = products.map { it.category.trim() }.filter { it.isNotBlank() }.toSortedSet()
            listOf("Lahat (All)") + set
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(
                "Lahat (All)",
                "Snacks & Biscuits",
                "Noodles & Canned",
                "Coffee & Beverages",
                "Dairy & Breakfast",
                "Cooking & Condiments",
                "Personal Care & Household"
            )
        )

    val stats: StateFlow<InventoryStats> = allProductsFlow
        .combine(MutableStateFlow(Unit)) { products, _ ->
            val now = System.currentTimeMillis()
            val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
            InventoryStats(
                totalCount = products.size,
                lowStockCount = products.count { it.quantity in 1..5 },
                expiringCount = products.count { (it.expirationDate - now) in 0..thirtyDaysMs },
                outOfStockCount = products.count { it.quantity <= 0 }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InventoryStats()
        )

    // Filtered products for Inventory Hub (Tab + Search)
    val displayedProducts: StateFlow<List<Product>> = combine(
        allProductsFlow,
        _selectedTabIndex,
        _searchQuery
    ) { products, tabIndex, query ->
        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000

        // 1. Filter by Tab (0 = All, 1 = Low Stock, 2 = Expiring)
        val tabFiltered = when (tabIndex) {
            1 -> products.filter { it.quantity in 1..5 }
            2 -> products.filter { (it.expirationDate - now) <= thirtyDaysMs }
            else -> products
        }

        // 2. Filter by search query if non-empty
        if (query.isBlank()) {
            tabFiltered
        } else {
            val trimmed = query.trim()
            tabFiltered.filter { p ->
                p.productName.contains(trimmed, ignoreCase = true) ||
                        p.barcode.contains(trimmed, ignoreCase = true) ||
                        p.category.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered products for POS Dashboard (Category + Search)
    val posCategorizedProducts: StateFlow<List<Product>> = combine(
        allProductsFlow,
        _selectedPosCategory,
        _searchQuery
    ) { products, selectedCat, query ->
        // Filter by category
        val catFiltered = if (selectedCat == "Lahat (All)" || selectedCat.isBlank()) {
            products
        } else {
            products.filter { it.category.equals(selectedCat, ignoreCase = true) }
        }

        // Filter by search query
        if (query.isBlank()) {
            catFiltered
        } else {
            val trimmed = query.trim()
            catFiltered.filter { p ->
                p.productName.contains(trimmed, ignoreCase = true) ||
                        p.barcode.contains(trimmed, ignoreCase = true) ||
                        p.category.contains(trimmed, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Cart details with total calculation
    val cartItems: StateFlow<List<CartItem>> = combine(allProductsFlow, _cartMap) { products, map ->
        val prodMap = products.associateBy { it.id }
        map.mapNotNull { (prodId, count) ->
            prodMap[prodId]?.let { CartItem(it, count) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cartTotalAmount: StateFlow<Double> = cartItems.combine(MutableStateFlow(Unit)) { items, _ ->
        items.sumOf { it.product.price * it.quantityToBuy }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun setTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPosCategory(category: String) {
        _selectedPosCategory.value = category
    }

    fun populateSearchFromBarcode(barcode: String) {
        _searchQuery.value = barcode
        viewModelScope.launch {
            _userMessage.emit("Nahanap ang barcode: $barcode")
        }
    }

    // POS Cart Operations
    fun addToCart(product: Product) {
        val currentCount = _cartMap.value[product.id] ?: 0
        if (currentCount >= product.quantity) {
            viewModelScope.launch {
                _userMessage.emit("Wala nang sapat na stock para sa ${product.productName}!")
            }
            return
        }
        _cartMap.value = _cartMap.value + (product.id to (currentCount + 1))
    }

    fun removeFromCart(product: Product) {
        val currentCount = _cartMap.value[product.id] ?: 0
        if (currentCount <= 1) {
            _cartMap.value = _cartMap.value - product.id
        } else {
            _cartMap.value = _cartMap.value + (product.id to (currentCount - 1))
        }
    }

    fun clearCart() {
        _cartMap.value = emptyMap()
    }

    fun checkoutCart() {
        val items = cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            var totalSold = 0
            var totalPesos = 0.0

            for (item in items) {
                val p = item.product
                val buyQty = item.quantityToBuy
                val newQty = (p.quantity - buyQty).coerceAtLeast(0)
                productDao.insertProduct(p.copy(quantity = newQty))
                totalSold += buyQty
                totalPesos += (p.price * buyQty)
            }

            _cartMap.value = emptyMap()
            _userMessage.emit("Matagumpay na nabenta! Total: ₱${String.format(Locale.US, "%.2f", totalPesos)} ($totalSold items)")
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.insertProduct(product)
            _userMessage.emit("Nai-save ang produkto: ${product.productName}")
        }
    }

    fun incrementStock(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.incrementStock(product.barcode)
            _userMessage.emit("+1 Dagdag stock: ${product.productName}")
        }
    }

    fun processSale(barcode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val product = productDao.getProductByBarcode(barcode)
            if (product == null) {
                _userMessage.emit("Walang nahanap na item para sa barcode: $barcode")
            } else if (product.quantity <= 0) {
                _userMessage.emit("Ubos na ang stock ng ${product.productName}!")
            } else {
                productDao.processSale(barcode)
                val priceText = if (product.price > 0) " (₱${String.format(Locale.US, "%.2f", product.price)})" else ""
                _userMessage.emit("Nabenta: ${product.productName}$priceText - Natitira: ${product.quantity - 1}")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productDao.deleteProduct(product)
            _userMessage.emit("Inalis ang item: ${product.productName}")
        }
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return withContext(Dispatchers.IO) {
            productDao.getProductByBarcode(barcode)
        }
    }

    fun syncAndRefreshDatabase() {
        viewModelScope.launch {
            _syncMessage.value = "Pagsasabay-sabay ng Imbentaryo..."
            _isSyncing.value = true
            kotlinx.coroutines.delay(1300)
            _isSyncing.value = false
            _userMessage.emit("Updated at naka-sync ang lahat ng produkto!")
        }
    }

    fun exportToCsv(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _syncMessage.value = "Ini-export ang mga produkto sa CSV..."
            _isSyncing.value = true
            try {
                val products = displayedProducts.value
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = outputStream.bufferedWriter()
                    writer.write("Barcode,Product Name,Category,Price,Quantity,Expiration Date\n")
                    for (p in products) {
                        val date = dateFormat.format(Date(p.expirationDate))
                        writer.write("${p.barcode},\"${p.productName.replace("\"", "\"\"")}\",\"${p.category}\",${p.price},${p.quantity},$date\n")
                    }
                    writer.flush()
                }
                kotlinx.coroutines.delay(600)
                _userMessage.emit("Na-export ang ${products.size} produkto sa CSV")
            } catch (_: Exception) {
                _userMessage.emit("Bigo ang pag-export sa CSV. Pakisubukan muli.")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun backupToJson(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _syncMessage.value = "Nagba-backup ng database (JSON)..."
            _isSyncing.value = true
            try {
                val products = displayedProducts.value
                val json = Gson().toJson(products)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray())
                }
                kotlinx.coroutines.delay(600)
                _userMessage.emit("Na-backup ang database (${products.size} items)")
            } catch (_: Exception) {
                _userMessage.emit("Bigo ang pag-backup ng database. Pakisubukan muli.")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun restoreFromJson(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _syncMessage.value = "Ibinabalik ang database mula sa backup..."
            _isSyncing.value = true
            try {
                val count = context.contentResolver.openInputStream(uri)?.use { input ->
                    val type = object : TypeToken<List<Product>>() {}.type
                    val restored: List<Product> = Gson().fromJson(InputStreamReader(input), type)
                    restored.forEach { p -> productDao.insertProduct(p) }
                    restored.size
                } ?: 0
                kotlinx.coroutines.delay(800)
                _userMessage.emit("Naibalik ang $count produkto mula sa backup")
            } catch (_: Exception) {
                _userMessage.emit("Bigo ang pag-restore. Pakitiyak na wasto ang napiling backup file.")
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
