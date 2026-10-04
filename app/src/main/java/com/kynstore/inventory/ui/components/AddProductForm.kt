package com.kynstore.inventory.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynstore.inventory.data.Product
import com.kynstore.inventory.ui.theme.BlueBorder
import com.kynstore.inventory.ui.theme.BlueContainer
import com.kynstore.inventory.ui.theme.BlueDark
import com.kynstore.inventory.ui.theme.BlueLightPill
import com.kynstore.inventory.ui.theme.BluePrimary
import com.kynstore.inventory.ui.theme.CardWhite
import com.kynstore.inventory.ui.theme.SoftBlueBackground
import com.kynstore.inventory.ui.theme.TextDark
import com.kynstore.inventory.ui.theme.TextMuted
import com.kynstore.inventory.ui.theme.TextSubtle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductForm(
    scannedBarcode: String,
    onSave: (Product) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var barcode by remember { mutableStateOf(scannedBarcode) }
    var productName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(10) }
    var priceText by remember { mutableStateOf("15.00") }
    var selectedCategory by remember { mutableStateOf("Snacks & Biscuits") }

    val categoriesList = listOf(
        "Snacks & Biscuits",
        "Noodles & Canned",
        "Coffee & Beverages",
        "Dairy & Breakfast",
        "Cooking & Condiments",
        "Personal Care & Household",
        "Iba Pa (Other)"
    )

    // Expiration date calculation (default: 3 months from now)
    val now = remember { System.currentTimeMillis() }
    val oneDayMs = 24L * 60 * 60 * 1000
    var selectedExpirationDate by remember { mutableLongStateOf(now + (90L * oneDayMs)) }
    var selectedPreset by remember { mutableIntStateOf(3) }

    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Magdagdag ng Paninda",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("add_product_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Bumalik",
                            tint = Color.White
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barcode Card Header
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BlueBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BlueContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Barcode Number ng Produkto",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = barcode,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                }
            }

            // Category Selection (Essential for categorization)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Piliin ang Kategorya (Category)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoriesList.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = CardWhite,
                                labelColor = TextDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BluePrimary else BlueBorder,
                                borderWidth = 1.5.dp
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Product Name Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Pangalan ng Paninda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    placeholder = { Text("hal. Piattos Cheese 40g, Lucky Me Canton...", color = TextMuted) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextDark, fontWeight = FontWeight.Bold),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = BluePrimary)
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("Kailangan ilagay ang pangalan ng produkto!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = BlueBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input")
                )
            }

            // Price Field (Philippine Pesos ₱)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Presyo (Benta sa Tindahan)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Presyo bawat piraso") },
                    prefix = { Text("₱ ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = BluePrimary) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = BluePrimary, fontWeight = FontWeight.ExtraBold),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = BluePrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = BlueBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_price_input")
                )
            }

            // Initial Stock Stepper
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Bilang ng Stock (Quantity)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BlueBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Dami ng piraso:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 0) quantity-- },
                                modifier = Modifier.testTag("qty_minus_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Bawasan", tint = BlueDark)
                            }

                            Text(
                                text = "$quantity pcs",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = BluePrimary,
                                modifier = Modifier.testTag("qty_text")
                            )

                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.testTag("qty_plus_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Dagdagan", tint = BlueDark)
                            }
                        }
                    }
                }
            }

            // Expiration Date
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Petsa ng Expiration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BlueBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mage-expire: ${dateFormat.format(Date(selectedExpirationDate))}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val presets = listOf(
                                "1 Buwan" to 1,
                                "3 Buwan" to 3,
                                "6 Buwan" to 6,
                                "1 Taon" to 12
                            )
                            presets.forEach { (label, months) ->
                                FilterChip(
                                    selected = selectedPreset == months,
                                    onClick = {
                                        selectedPreset = months
                                        val cal = Calendar.getInstance().apply {
                                            timeInMillis = now
                                            add(Calendar.MONTH, months)
                                        }
                                        selectedExpirationDate = cal.timeInMillis
                                    },
                                    label = { Text(label, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BluePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Large and clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, BluePrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("cancel_add_product_button")
                ) {
                    Text(
                        text = "Kanselahin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }

                Button(
                    onClick = {
                        if (productName.isBlank()) {
                            nameError = true
                        } else {
                            val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
                            val newProduct = Product(
                                barcode = barcode.trim(),
                                productName = productName.trim(),
                                quantity = quantity,
                                expirationDate = selectedExpirationDate,
                                category = selectedCategory,
                                price = parsedPrice
                            )
                            onSave(newProduct)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("save_product_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I-SAVE ITO",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
