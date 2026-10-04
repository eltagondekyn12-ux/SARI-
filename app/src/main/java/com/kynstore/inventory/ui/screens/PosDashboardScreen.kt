package com.kynstore.inventory.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.kynstore.inventory.util.AppLanguage
import com.kynstore.inventory.util.AppLanguageManager
import com.kynstore.inventory.util.rememberAppLanguage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynstore.inventory.data.Product
import com.kynstore.inventory.ui.components.CloudSyncLoadingOverlay
import com.kynstore.inventory.ui.theme.BlueBorder
import com.kynstore.inventory.ui.theme.BlueContainer
import com.kynstore.inventory.ui.theme.BlueDark
import com.kynstore.inventory.ui.theme.BlueLightPill
import com.kynstore.inventory.ui.theme.BluePrimary
import com.kynstore.inventory.ui.theme.CardWhite
import com.kynstore.inventory.ui.theme.SoftBlueBackground
import com.kynstore.inventory.ui.theme.StatusGreen
import com.kynstore.inventory.ui.theme.StatusGreenContainer
import com.kynstore.inventory.ui.theme.StatusOrange
import com.kynstore.inventory.ui.theme.StatusOrangeContainer
import com.kynstore.inventory.ui.theme.StatusRed
import com.kynstore.inventory.ui.theme.StatusRedContainer
import com.kynstore.inventory.ui.theme.TextDark
import com.kynstore.inventory.ui.theme.TextMuted
import com.kynstore.inventory.ui.theme.TextSubtle
import com.kynstore.inventory.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosDashboardScreen(
    viewModel: InventoryViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToSearchScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val products by viewModel.posCategorizedProducts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedPosCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val totalAmount by viewModel.cartTotalAmount.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

    var showCartDialog by remember { mutableStateOf(false) }

    val currentLang = rememberAppLanguage()
    val addLabel = AppLanguageManager.getAddLabel(currentLang)

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tindahan Cashier / POS",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Pindutin ang item para ibenta",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                actions = {
                    // Fast sync / refresh button
                    IconButton(
                        onClick = { viewModel.syncAndRefreshDatabase() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pos_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "I-sync ang Imbentaryo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Fast Barcode Scan button in header
                    IconButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pos_header_scanner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "I-scan ang Barcode",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = SoftBlueBackground
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
            ) {
            // Search Input with Barcode button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        "Maghanap ng paninda o barcode...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextDark, fontWeight = FontWeight.SemiBold),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Linisin ang search",
                                    tint = TextDark
                                )
                            }
                        }
                        IconButton(
                            onClick = onNavigateToSearchScanner,
                            modifier = Modifier.testTag("pos_search_barcode_scanner")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "I-scan para hanapin",
                                tint = BluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardWhite,
                    unfocusedContainerColor = CardWhite,
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = BlueBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("pos_search_bar")
            )

            // Category Selector (Large, touch-friendly pills for elders)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = category.equals(selectedCategory, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (isSelected) BluePrimary else CardWhite,
                        shadowElevation = if (isSelected) 3.dp else 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.5.dp,
                                color = if (isSelected) BluePrimary else BlueBorder,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.setPosCategory(category) }
                            .testTag("category_pill_${category.replace(" ", "_")}")
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else TextDark,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Categorized Items List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BlueContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Walang nahanap na paninda",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Piliin ang ibang kategorya o i-scan ang bagong produkto.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pos_products_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val countInCart = cartItems.find { it.product.id == product.id }?.quantityToBuy ?: 0
                        PosProductCard(
                            product = product,
                            countInCart = countInCart,
                            addLabel = addLabel,
                            onQuickSellOne = { viewModel.processSale(product.barcode) },
                            onAddToCart = { viewModel.addToCart(product) },
                            onRemoveFromCart = { viewModel.removeFromCart(product) }
                        )
                    }
                }
            }

            // Bottom Sticky Cashier Cart Bar (Visible whenever items are in cart)
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = BlueDark,
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable { showCartDialog = true }
                                .weight(1f)
                        ) {
                            Text(
                                text = "KASALUKUYANG BENTA (${cartItems.sumOf { it.quantityToBuy }} items)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "₱${String.format(Locale.US, "%.2f", totalAmount)}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF67E8F9) // Bright cyan-blue
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showCartDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tingnan", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.checkoutCart() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp).testTag("pos_checkout_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IBENTA NA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Cart Details Dialog
    if (showCartDialog) {
        AlertDialog(
            onDismissRequest = { showCartDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Listahan ng Binili", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.productName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "₱${String.format(Locale.US, "%.2f", item.product.price)} x ${item.quantityToBuy} pcs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSubtle
                                )
                            }
                            Text(
                                text = "₱${String.format(Locale.US, "%.2f", item.product.price * item.quantityToBuy)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = BluePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueContainer, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kabuuang Halaga:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("₱${String.format(Locale.US, "%.2f", totalAmount)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = BlueDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.checkoutCart()
                        showCartDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    modifier = Modifier.testTag("pos_dialog_checkout_button")
                ) {
                    Text("Kumpletuhin ang Benta", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCart()
                        showCartDialog = false
                    }
                ) {
                    Text("Kanselahin / Alisin Lahat", color = StatusRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Custom Uiverse Cloud Sync Loading Overlay
    CloudSyncLoadingOverlay(
        isLoading = isSyncing,
        message = syncMessage
    )
}

@Composable
private fun PosProductCard(
    product: Product,
    countInCart: Int,
    addLabel: String = "Dagdag",
    onQuickSellOne: () -> Unit,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit
) {
    val isOutOfStock = product.quantity <= 0
    val isLowStock = product.quantity in 1..5

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BlueBorder, RoundedCornerShape(16.dp))
            .testTag("pos_product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category tag and Stock status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Surface(
                    color = BlueLightPill,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = BlueDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Stock Badge (Filipino + English)
                Surface(
                    color = when {
                        isOutOfStock -> StatusRedContainer
                        isLowStock -> StatusOrangeContainer
                        else -> StatusGreenContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            isOutOfStock -> "UBOS NA (0 pcs)"
                            isLowStock -> "PAUBOS NA (${product.quantity} pcs)"
                            else -> "MAY STOCK: ${product.quantity} pcs"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isOutOfStock -> StatusRed
                            isLowStock -> StatusOrange
                            else -> StatusGreen
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Large Product Name & Price in Pesos (₱)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Barcode: ${product.barcode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                // Large Price in Pesos
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₱${String.format(Locale.US, "%.2f", product.price)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = BluePrimary
                    )
                    Text(
                        text = "bawat piraso",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Large and easy to tap for elders
            val currentLang = rememberAppLanguage()
            val sellLabel = when (currentLang) {
                AppLanguage.ENGLISH -> "Sell 1"
                AppLanguage.TAGALOG -> "Benta 1"
                AppLanguage.BISAYA -> "Baligya 1"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Sell 1 Button (Immediate single sale)
                Button(
                    onClick = onQuickSellOne,
                    enabled = !isOutOfStock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isOutOfStock) BlueDark else Color(0xFF94A3B8),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("pos_quick_sell_${product.id}")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = sellLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Cart Add / Stepper
                if (countInCart == 0) {
                    OutlinedButton(
                        onClick = onAddToCart,
                        enabled = !isOutOfStock,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (!isOutOfStock) BluePrimary else BlueBorder),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pos_add_cart_${product.id}")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = addLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    // Stepper: Minus, Count, Plus
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(BlueContainer, RoundedCornerShape(12.dp))
                            .border(1.5.dp, BluePrimary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onRemoveFromCart,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Bawasan", tint = BlueDark)
                        }
                        Text(
                            text = "$countInCart",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueDark
                        )
                        IconButton(
                            onClick = onAddToCart,
                            enabled = countInCart < product.quantity,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Dagdagan", tint = BlueDark)
                        }
                    }
                }
            }
        }
    }
}
