package com.kynstore.inventory.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynstore.inventory.ui.components.AddProductForm
import com.kynstore.inventory.ui.screens.AnimatedSplashScreen
import com.kynstore.inventory.ui.screens.CameraPermissionManager
import com.kynstore.inventory.ui.screens.CameraScannerScreen
import com.kynstore.inventory.ui.screens.InventoryHubScreen
import com.kynstore.inventory.ui.screens.LandingScreen
import com.kynstore.inventory.ui.screens.PosDashboardScreen
import com.kynstore.inventory.util.AppStrings
import com.kynstore.inventory.util.LanguageManager
import com.kynstore.inventory.ui.theme.BlueBorder
import com.kynstore.inventory.ui.theme.BlueContainer
import com.kynstore.inventory.ui.theme.BlueDark
import com.kynstore.inventory.ui.theme.BluePrimary
import com.kynstore.inventory.ui.theme.CardWhite
import com.kynstore.inventory.ui.theme.TextDark
import com.kynstore.inventory.ui.theme.TextMuted
import com.kynstore.inventory.ui.viewmodel.InventoryViewModel

@Composable
fun SariSariAppNavigation(viewModel: InventoryViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "splash") {
        // --- SCREEN 1: SPLASH SCREEN ---
        composable("splash") {
            AnimatedSplashScreen(
                onSplashFinished = {
                    val prefs = context.getSharedPreferences("kyn_store_prefs", Context.MODE_PRIVATE)
                    val hasAgreed = prefs.getBoolean("has_agreed_terms", false)
                    val targetDestination = if (hasAgreed) "main" else "landing"
                    navController.navigate(targetDestination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // --- SCREEN 2: LANDING PAGE WITH TERMS AGREEMENT & 3S PREPARATION ---
        composable("landing") {
            LandingScreen(
                onStartApp = {
                    navController.navigate("main") {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }

        // --- SCREEN 3: MAIN SCREEN (CATEGORIZED POS + INVENTORY DASHBOARDS) ---
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onNavigateToScanner = { navController.navigate("scanner") },
                onNavigateToSearchScanner = { navController.navigate("search_scanner") },
                onNavigateToLanding = { navController.navigate("landing") }
            )
        }

        // Backward compatibility route
        composable("inventory") {
            MainScreen(
                viewModel = viewModel,
                onNavigateToScanner = { navController.navigate("scanner") },
                onNavigateToSearchScanner = { navController.navigate("search_scanner") }
            )
        }

        // --- SCREEN 3: CAMERA SCANNER (ADD PRODUCT) ---
        composable("scanner") {
            var scannedCode by remember { mutableStateOf<String?>(null) }

            BackHandler(enabled = true) {
                if (scannedCode != null) {
                    scannedCode = null
                } else {
                    navController.popBackStack()
                }
            }

            if (scannedCode == null) {
                CameraPermissionManager(
                    onManualBarcode = { code ->
                        scannedCode = code
                    }
                ) {
                    CameraScannerScreen(
                        scannerTitle = "I-scan ang Barcode",
                        subtitle = "Detects EAN-13, EAN-8, UPC-A, UPC-E",
                        isSearchMode = false,
                        onBarcodeScanned = { code ->
                            scannedCode = code
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            } else {
                AddProductForm(
                    scannedBarcode = scannedCode!!,
                    onSave = { newProduct ->
                        viewModel.addProduct(newProduct)
                        navController.popBackStack()
                    },
                    onCancel = {
                        scannedCode = null
                        navController.popBackStack()
                    }
                )
            }
        }

        // --- SCREEN 4: CAMERA SCANNER (SEARCH MODE) ---
        composable("search_scanner") {
            CameraPermissionManager(
                onManualBarcode = { code ->
                    viewModel.populateSearchFromBarcode(code)
                    navController.popBackStack()
                }
            ) {
                CameraScannerScreen(
                    scannerTitle = "I-scan ang Barcode para Hanapin",
                    subtitle = "Detects EAN-13, EAN-8, UPC-A, UPC-E",
                    isSearchMode = true,
                    onBarcodeScanned = { code ->
                        viewModel.populateSearchFromBarcode(code)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * MainScreen provides an elder-friendly, high-contrast White and Blue bottom navigation bar
 * allowing seniors to easily switch between the Categorized POS Dashboard and the Inventory Hub.
 */
@Composable
fun MainScreen(
    viewModel: InventoryViewModel,
    onNavigateToScanner: () -> Unit,
    onNavigateToSearchScanner: () -> Unit,
    onNavigateToLanding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsStateWithLifecycle()
    val strings = remember(currentLang) { AppStrings.forLanguage(currentLang) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = POS Cashier, 1 = Inventory Stock

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardWhite,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(BorderStroke(1.dp, BlueBorder))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(72.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = "POS Cashier",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = strings.navPosTitle,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                            fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BlueDark,
                        indicatorColor = BlueContainer,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_pos_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Inventory Stock",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = strings.navInventoryTitle,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                            fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BluePrimary,
                        selectedTextColor = BlueDark,
                        indicatorColor = BlueContainer,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_inventory_tab")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == 0) {
                PosDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToScanner = onNavigateToScanner,
                    onNavigateToSearchScanner = onNavigateToSearchScanner
                )
            } else {
                InventoryHubScreen(
                    viewModel = viewModel,
                    onNavigateToScanner = onNavigateToScanner,
                    onNavigateToSearchScanner = onNavigateToSearchScanner,
                    onNavigateToTerms = onNavigateToLanding
                )
            }
        }
    }
}
