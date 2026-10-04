package com.kynstore.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kynstore.inventory.data.InventoryDatabase
import com.kynstore.inventory.navigation.SariSariAppNavigation
import com.kynstore.inventory.ui.theme.KYNStoreTheme
import com.kynstore.inventory.ui.viewmodel.InventoryViewModel

import com.kynstore.inventory.util.LanguageManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent Language Manager
        LanguageManager.init(this)

        // 1. Initialize the local database
        val database = InventoryDatabase.getDatabase(this)

        setContent {
            KYNStoreTheme {
                // 2. Initialize the ViewModel with the DAO
                val viewModel: InventoryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return InventoryViewModel(database.productDao()) as T
                        }
                    }
                )

                // 3. Launch the Navigation Graph
                SariSariAppNavigation(viewModel = viewModel)
            }
        }
    }
}
