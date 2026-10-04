package com.kynstore.inventory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a Sari-Sari store inventory product.
 * Categorized for both inventory management and POS cashier dashboard.
 */
@Entity(tableName = "products", indices = [Index(value = ["barcode"], unique = true)])
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "barcode") val barcode: String,
    @ColumnInfo(name = "product_name") val productName: String,
    @ColumnInfo(name = "quantity") var quantity: Int,
    @ColumnInfo(name = "expiration_date") val expirationDate: Long,
    @ColumnInfo(name = "category", defaultValue = "Snacks & Biscuits") val category: String = "Snacks & Biscuits",
    @ColumnInfo(name = "price", defaultValue = "0.0") val price: Double = 0.0
)
