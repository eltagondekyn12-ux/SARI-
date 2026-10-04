package com.kynstore.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Product::class], version = 2, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        /**
         * Explicit non-destructive migration from version 1 to 2.
         * Protects and preserves all existing inventory items, quantities, and barcodes
         * so no data is ever erased or destroyed during an application update.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE products ADD COLUMN price REAL NOT NULL DEFAULT 0.0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_products_barcode ON products (barcode)")
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "sarisari_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedDatabase(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Seed when database is opened and empty
                try {
                    db.query("SELECT COUNT(*) FROM products").use { cursor ->
                        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                            seedDatabase(db)
                        }
                    }
                } catch (_: Exception) {}
            }

            private fun seedDatabase(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                val oneDay = 24L * 60 * 60 * 1000

                data class SeedItem(
                    val barcode: String,
                    val name: String,
                    val qty: Int,
                    val exp: Long,
                    val category: String,
                    val price: Double
                )

                val seedData = listOf(
                    SeedItem("4800016641203", "Piattos Cheese 40g", 14, now + 45L * oneDay, "Snacks & Biscuits", 18.00),
                    SeedItem("4800016053013", "Lucky Me! Pancit Canton Kalamansi", 3, now + 12L * oneDay, "Noodles & Canned", 16.00),
                    SeedItem("8996001414002", "Kopiko Blanca 30g 10s", 8, now + 90L * oneDay, "Coffee & Beverages", 12.00),
                    SeedItem("4800361284728", "Bear Brand Fortified Milk 33g", 2, now + 18L * oneDay, "Dairy & Breakfast", 15.00),
                    SeedItem("4800119001010", "Datu Puti White Vinegar 350ml", 16, now + 180L * oneDay, "Cooking & Condiments", 22.00),
                    SeedItem("4800119002017", "Silver Swan Soy Sauce 385ml", 6, now + 150L * oneDay, "Cooking & Condiments", 24.00),
                    SeedItem("4902430489950", "Safeguard Pure White Bar 60g", 0, now + 240L * oneDay, "Personal Care & Household", 28.00),
                    SeedItem("4800016552011", "SkyFlakes Crackers 25g 10s", 4, now + 8L * oneDay, "Snacks & Biscuits", 10.00),
                    SeedItem("4800016111119", "San Miguel Pale Pilsen 330ml", 12, now + 120L * oneDay, "Coffee & Beverages", 55.00),
                    SeedItem("4800016222228", "555 Sardines in Tomato Sauce 155g", 8, now + 200L * oneDay, "Noodles & Canned", 25.00)
                )

                for (item in seedData) {
                    val barcode = item.barcode
                    val name = item.name.replace("'", "''")
                    val qty = item.qty
                    val exp = item.exp
                    val cat = item.category.replace("'", "''")
                    val price = item.price
                    db.execSQL(
                        "INSERT OR IGNORE INTO products (barcode, product_name, quantity, expiration_date, category, price) " +
                                "VALUES ('$barcode', '$name', $qty, $exp, '$cat', $price)"
                    )
                }
            }
        }
    }
}
