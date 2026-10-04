package com.kynstore.inventory.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import com.kynstore.inventory.ui.components.LanguageSelectionDialog
import com.kynstore.inventory.util.AppLanguage
import com.kynstore.inventory.util.AppLanguageManager
import com.kynstore.inventory.util.rememberAppLanguage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynstore.inventory.data.Product
import com.kynstore.inventory.ui.components.CloudSyncLoadingOverlay
import com.kynstore.inventory.ui.components.SwipeableInventoryCard
import com.kynstore.inventory.ui.theme.BlueBorder
import com.kynstore.inventory.ui.theme.BlueContainer
import com.kynstore.inventory.ui.theme.BlueDark
import com.kynstore.inventory.ui.theme.BluePrimary
import com.kynstore.inventory.ui.theme.CardWhite
import com.kynstore.inventory.ui.theme.SoftBlueBackground
import com.kynstore.inventory.ui.theme.StatusGreen
import com.kynstore.inventory.ui.theme.StatusOrange
import com.kynstore.inventory.ui.theme.StatusRed
import com.kynstore.inventory.ui.theme.TextDark
import com.kynstore.inventory.ui.theme.TextMuted
import com.kynstore.inventory.ui.theme.TextSubtle
import com.kynstore.inventory.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryHubScreen(
    viewModel: InventoryViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToSearchScanner: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val displayedProducts by viewModel.displayedProducts.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showPosDialog by remember { mutableStateOf(false) }
    var posBarcodeQuery by remember { mutableStateOf("") }
    val currentLang = rememberAppLanguage()
    val addLabel = AppLanguageManager.getAddLabel(currentLang)
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Storage Access Framework Launchers for CSV and JSON (from spec Section 6 & 7)
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.exportToCsv(context, it) }
    }

    val backupJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.backupToJson(context, it) }
    }

    val restoreJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.restoreFromJson(context, it) }
    }

    // Hardware Bluetooth / USB Scanner Support (Section 5 from spec)
    var barcodeBuffer by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Listen for feedback messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Hardware scanner focus on start
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                    if (barcodeBuffer.isNotEmpty()) {
                        // Quick POS deduct on barcode gun scan
                        viewModel.processSale(barcodeBuffer)
                        barcodeBuffer = ""
                    }
                    return@onKeyEvent true
                } else if (keyEvent.type == KeyEventType.KeyUp) {
                    val unicode = keyEvent.nativeKeyEvent.unicodeChar
                    if (unicode != 0) {
                        val char = unicode.toChar()
                        if (char.isLetterOrDigit()) {
                            barcodeBuffer += char
                        }
                    }
                }
                false
            }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = BluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "KYN Imbentaryo",
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Subaybayan ang Stock at Expiration",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    },
                    actions = {
                        // POS Quick Sale button
                        IconButton(
                            onClick = { showPosDialog = true },
                            modifier = Modifier.testTag("pos_quick_sale_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = "Quick Sale / POS",
                                tint = Color.White
                            )
                        }

                        // More Actions Menu
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("more_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("I-Sync / I-Refresh ang Tindahan", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    viewModel.syncAndRefreshDatabase()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = BluePrimary)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Export to CSV", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    exportCsvLauncher.launch("kyn_store_inventory_${System.currentTimeMillis()}.csv")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = BluePrimary)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Backup Database (JSON)", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    backupJsonLauncher.launch("kyn_store_backup_${System.currentTimeMillis()}.json")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = BluePrimary)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Restore Database (JSON)", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    restoreJsonLauncher.launch(arrayOf("application/json", "text/*"))
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = BluePrimary)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Mga Tuntunin at Privacy Policy", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    onNavigateToTerms()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = BluePrimary)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (currentLang) {
                                            AppLanguage.ENGLISH -> "Change Language"
                                            AppLanguage.TAGALOG -> "Palitan ang Wika"
                                            AppLanguage.BISAYA -> "Usba ang Pinulongan"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showLanguageDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = BluePrimary)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BluePrimary,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = SoftBlueBackground,
            // Centered FAB permanently anchored above the list per specification
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToScanner,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = addLabel
                        )
                    },
                    text = {
                        Text(
                            text = addLabel,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("scan_barcode_fab")
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                ) {
                // Store KPI Stats Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = when (currentLang) {
                            AppLanguage.ENGLISH -> "Total"
                            AppLanguage.TAGALOG -> "Kabuuan"
                            AppLanguage.BISAYA -> "Tanan"
                        },
                        value = "${stats.totalCount}",
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = when (currentLang) {
                            AppLanguage.ENGLISH -> "Low Stock"
                            AppLanguage.TAGALOG -> "Paubos"
                            AppLanguage.BISAYA -> "Hapit Na"
                        },
                        value = "${stats.lowStockCount}",
                        accentColor = StatusOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = when (currentLang) {
                            AppLanguage.ENGLISH -> "Expiring"
                            AppLanguage.TAGALOG -> "Pa-expire"
                            AppLanguage.BISAYA -> "Pa-expire"
                        },
                        value = "${stats.expiringCount}",
                        accentColor = StatusOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = when (currentLang) {
                            AppLanguage.ENGLISH -> "Out of Stock"
                            AppLanguage.TAGALOG -> "Ubos Na"
                            AppLanguage.BISAYA -> "Nahurot"
                        },
                        value = "${stats.outOfStockCount}",
                        accentColor = StatusRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            when (currentLang) {
                                AppLanguage.ENGLISH -> "Search by product name or barcode..."
                                AppLanguage.TAGALOG -> "Maghanap ng paninda o barcode..."
                                AppLanguage.BISAYA -> "Pangitaa ang baligya o barcode..."
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                            IconButton(
                                onClick = onNavigateToSearchScanner,
                                modifier = Modifier.testTag("barcode_search_scanner_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan barcode to search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("inventory_search_bar")
                )

                // TabRow Placed Directly Above the LazyColumn (0 = All, 1 = Low Stock, 2 = Expiring)
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabTitles = listOf(
                        when (currentLang) {
                            AppLanguage.ENGLISH -> "All Items (${stats.totalCount})"
                            AppLanguage.TAGALOG -> "Lahat (${stats.totalCount})"
                            AppLanguage.BISAYA -> "Tanan (${stats.totalCount})"
                        },
                        when (currentLang) {
                            AppLanguage.ENGLISH -> "Low Stock (${stats.lowStockCount})"
                            AppLanguage.TAGALOG -> "Paubos (${stats.lowStockCount})"
                            AppLanguage.BISAYA -> "Hapit Na (${stats.lowStockCount})"
                        },
                        when (currentLang) {
                            AppLanguage.ENGLISH -> "Expiring (${stats.expiringCount})"
                            AppLanguage.TAGALOG -> "Pa-expire (${stats.expiringCount})"
                            AppLanguage.BISAYA -> "Pa-expire (${stats.expiringCount})"
                        }
                    )

                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { viewModel.setTabIndex(index) },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("inventory_tab_$index")
                        )
                    }
                }

                // Inventory List
                if (displayedProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = when (selectedTabIndex) {
                                    1 -> "No low stock products"
                                    2 -> "No products expiring within 30 days"
                                    else -> if (searchQuery.isNotEmpty()) "No matching products found" else "Inventory is empty"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Tap 'Scan Barcode' below to add items to your sari-sari store inventory.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("inventory_list"),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                    ) {
                        items(
                            items = displayedProducts,
                            key = { it.id }
                        ) { product ->
                            SwipeableInventoryCard(
                                product = product,
                                onIncrementStock = { p -> viewModel.incrementStock(p) },
                                onDeleteProduct = { p -> viewModel.deleteProduct(p) },
                                onQuickSale = { p -> viewModel.processSale(p.barcode) }
                            )
                        }
                    }
                }
            }
        }
    }

        // Language Switcher Dialog
        if (showLanguageDialog) {
            LanguageSelectionDialog(onDismiss = { showLanguageDialog = false })
        }

        // Quick POS Sale Dialog
        if (showPosDialog) {
            AlertDialog(
                onDismissRequest = { showPosDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLang) {
                                AppLanguage.ENGLISH -> "Point of Sale (POS) Checkout"
                                AppLanguage.TAGALOG -> "POS Cashier Benta"
                                AppLanguage.BISAYA -> "POS Cashier Halin"
                            }
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = when (currentLang) {
                                AppLanguage.ENGLISH -> "Enter or scan a product barcode to deduct 1 unit from store stock:"
                                AppLanguage.TAGALOG -> "I-type o i-scan ang barcode upang magbawas ng 1 piraso sa stock:"
                                AppLanguage.BISAYA -> "I-type o i-scan ang barcode aron mokuha og 1 piraso sa stock:"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = posBarcodeQuery,
                            onValueChange = { posBarcodeQuery = it },
                            label = {
                                Text(
                                    when (currentLang) {
                                        AppLanguage.ENGLISH -> "Barcode Number"
                                        AppLanguage.TAGALOG -> "Numero ng Barcode"
                                        AppLanguage.BISAYA -> "Numero sa Barcode"
                                    }
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pos_barcode_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (posBarcodeQuery.isNotBlank()) {
                                viewModel.processSale(posBarcodeQuery.trim())
                                posBarcodeQuery = ""
                                showPosDialog = false
                            }
                        },
                        modifier = Modifier.testTag("pos_confirm_sale_button")
                    ) {
                        Text(
                            text = when (currentLang) {
                                AppLanguage.ENGLISH -> "Sell Item (-1)"
                                AppLanguage.TAGALOG -> "Ibenta ang Item (-1)"
                                AppLanguage.BISAYA -> "Ibaligya ang Item (-1)"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPosDialog = false }) {
                        Text(
                            text = when (currentLang) {
                                AppLanguage.ENGLISH -> "Cancel"
                                AppLanguage.TAGALOG -> "Kanselahin"
                                AppLanguage.BISAYA -> "Kanselahon"
                            }
                        )
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
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, BlueBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1
            )
        }
    }
}
