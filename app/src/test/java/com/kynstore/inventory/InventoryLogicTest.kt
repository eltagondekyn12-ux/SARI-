package com.kynstore.inventory

import com.kynstore.inventory.data.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryLogicTest {

    @Test
    fun testStockLevelFlags() {
        val inStockProduct = Product(
            id = 1,
            barcode = "12345",
            productName = "Piattos",
            quantity = 15,
            expirationDate = System.currentTimeMillis() + 100000000,
            category = "Snacks & Biscuits",
            price = 18.0
        )
        val lowStockProduct = Product(
            id = 2,
            barcode = "12346",
            productName = "Pancit Canton",
            quantity = 3,
            expirationDate = System.currentTimeMillis() + 100000000,
            category = "Noodles & Canned",
            price = 16.0
        )
        val outOfStockProduct = Product(
            id = 3,
            barcode = "12347",
            productName = "Safeguard",
            quantity = 0,
            expirationDate = System.currentTimeMillis() + 100000000,
            category = "Personal Care & Household",
            price = 28.0
        )

        assertFalse(inStockProduct.quantity in 1..5)
        assertTrue(lowStockProduct.quantity in 1..5)
        assertTrue(outOfStockProduct.quantity <= 0)
        assertEquals("Snacks & Biscuits", inStockProduct.category)
        assertEquals(18.0, inStockProduct.price, 0.001)
    }

    @Test
    fun testExpirationFlags() {
        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000

        val freshProduct = Product(
            id = 1,
            barcode = "111",
            productName = "Fresh Item",
            quantity = 10,
            expirationDate = now + (60L * 24 * 60 * 60 * 1000)
        )
        val expiringProduct = Product(
            id = 2,
            barcode = "222",
            productName = "Expiring Item",
            quantity = 10,
            expirationDate = now + (15L * 24 * 60 * 60 * 1000)
        )
        val expiredProduct = Product(
            id = 3,
            barcode = "333",
            productName = "Expired Item",
            quantity = 10,
            expirationDate = now - 1000
        )

        val freshRemaining = freshProduct.expirationDate - now
        val expiringRemaining = expiringProduct.expirationDate - now
        val expiredRemaining = expiredProduct.expirationDate - now

        assertFalse(freshRemaining <= thirtyDaysMs)
        assertTrue(expiringRemaining <= thirtyDaysMs && expiringRemaining > 0)
        assertTrue(expiredRemaining <= 0)
    }
}
