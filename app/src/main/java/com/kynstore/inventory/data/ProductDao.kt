package com.kynstore.inventory.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room database operations.
 * Reactive Flow queries push updates to Compose UI whenever stock changes.
 */
@Dao
interface ProductDao {
    // Exact match for barcode scanning
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    // Add or Update stock
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    // Deduct stock for Point of Sale (POS) Checkout
    @Query("UPDATE products SET quantity = quantity - 1 WHERE barcode = :barcode AND quantity > 0")
    suspend fun processSale(barcode: String)

    // Increment stock (+1)
    @Query("UPDATE products SET quantity = quantity + 1 WHERE barcode = :barcode")
    suspend fun incrementStock(barcode: String)

    // Retrieve full inventory, prioritized by expiration date
    @Query("SELECT * FROM products ORDER BY expiration_date ASC")
    fun getAllProducts(): Flow<List<Product>>

    // Search function for manual lookup
    @Query("SELECT * FROM products WHERE product_name LIKE '%' || :searchQuery || '%' OR barcode LIKE '%' || :searchQuery || '%'")
    fun searchProducts(searchQuery: String): Flow<List<Product>>

    // Remove product (Swipe-to-delete)
    @Delete
    suspend fun deleteProduct(product: Product)

    // Category queries for POS and Categorized Inventory
    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY product_name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)
}
