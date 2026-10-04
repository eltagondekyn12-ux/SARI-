package com.kynstore.inventory.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynstore.inventory.data.Product
import com.kynstore.inventory.ui.theme.BlueBorder
import com.kynstore.inventory.ui.theme.BlueContainer
import com.kynstore.inventory.ui.theme.BlueDark
import com.kynstore.inventory.ui.theme.BlueLightPill
import com.kynstore.inventory.ui.theme.BluePrimary
import com.kynstore.inventory.ui.theme.CardWhite
import com.kynstore.inventory.ui.theme.StatusGreen
import com.kynstore.inventory.ui.theme.StatusGreenContainer
import com.kynstore.inventory.ui.theme.StatusOrange
import com.kynstore.inventory.ui.theme.StatusOrangeContainer
import com.kynstore.inventory.ui.theme.StatusRed
import com.kynstore.inventory.ui.theme.StatusRedContainer
import com.kynstore.inventory.ui.theme.TextDark
import com.kynstore.inventory.ui.theme.TextMuted
import com.kynstore.inventory.ui.theme.TextSubtle
import com.kynstore.inventory.util.AppLanguage
import com.kynstore.inventory.util.AppLanguageManager
import com.kynstore.inventory.util.rememberAppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SwipeableInventoryCard(
    product: Product,
    onIncrementStock: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onQuickSale: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = rememberAppLanguage()
    val addLabel = AppLanguageManager.getAddLabel(lang)

    // Expiration and Stock Level Triggers
    val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
    val currentTime = System.currentTimeMillis()
    val timeUntilExpiration = product.expirationDate - currentTime

    // Expiration Flags
    val isExpiringSoon = timeUntilExpiration <= thirtyDaysMs && timeUntilExpiration > 0
    val isExpired = timeUntilExpiration <= 0

    // Stock Level Flags
    val isOutOfStock = product.quantity <= 0
    val isLowStock = product.quantity in 1..5

    // Status colors
    val statusColor = when {
        isExpired || isOutOfStock -> StatusRed
        isExpiringSoon || isLowStock -> StatusOrange
        else -> StatusGreen
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onIncrementStock(product)
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteProduct(product)
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("inventory_card_${product.id}"),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> StatusGreen
                    SwipeToDismissBoxValue.EndToStart -> StatusRed
                    else -> Color.Transparent
                },
                label = "swipe_bg_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                }
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = addLabel,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = addLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tanggalin (Delete)",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.5.dp, BlueBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // High-visibility left color indicator stripe
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .background(statusColor)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Category & Price Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = BlueLightPill,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BlueDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (product.price > 0) {
                            Text(
                                text = "₱${String.format(Locale.US, "%.2f", product.price)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = BluePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Title & Stock Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = BluePrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = product.barcode,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSubtle
                                )
                            }
                        }

                        // Stock Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isOutOfStock -> StatusRedContainer
                                isLowStock -> StatusOrangeContainer
                                else -> StatusGreenContainer
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${product.quantity}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor
                                )
                                Text(
                                    text = when {
                                        isOutOfStock -> when (lang) {
                                            AppLanguage.ENGLISH -> "OUT OF STOCK"
                                            AppLanguage.TAGALOG -> "UBOS NA"
                                            AppLanguage.BISAYA -> "NAHUROT NA"
                                        }
                                        isLowStock -> when (lang) {
                                            AppLanguage.ENGLISH -> "LOW STOCK"
                                            AppLanguage.TAGALOG -> "PAUBOS NA"
                                            AppLanguage.BISAYA -> "HAPIT NA"
                                        }
                                        else -> when (lang) {
                                            AppLanguage.ENGLISH -> "IN STOCK"
                                            AppLanguage.TAGALOG -> "MAY STOCK"
                                            AppLanguage.BISAYA -> "NAAY STOCK"
                                        }
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Expiration Pill Row: Dedicated line with plenty of space
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val expDateStr = dateFormat.format(Date(product.expirationDate))
                    val daysLeft = ((product.expirationDate - currentTime) / (24L * 60 * 60 * 1000)).toInt()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isExpired || isExpiringSoon) statusColor.copy(alpha = 0.12f)
                                else BlueLightPill
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpired || isExpiringSoon) Icons.Default.Warning else Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (isExpired || isExpiringSoon) statusColor else TextMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isExpired -> "Expired: $expDateStr"
                                isExpiringSoon -> when (lang) {
                                    AppLanguage.ENGLISH -> "Expiring: $daysLeft d left"
                                    AppLanguage.TAGALOG -> "Paubos na araw: $daysLeft d"
                                    AppLanguage.BISAYA -> "Hapit na ma-expire: $daysLeft d"
                                }
                                else -> "Exp: $expDateStr"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isExpired || isExpiringSoon) FontWeight.Bold else FontWeight.Medium,
                            color = if (isExpired || isExpiringSoon) statusColor else TextDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons Row: Full width, equal weights, generous elder-friendly touch targets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onQuickSale(product) },
                            enabled = product.quantity > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BlueDark
                            ),
                            border = BorderStroke(1.5.dp, if (product.quantity > 0) BluePrimary else BlueBorder),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("sell_button_${product.id}")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ENGLISH -> "Sell 1"
                                    AppLanguage.TAGALOG -> "Benta 1"
                                    AppLanguage.BISAYA -> "Baligya 1"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Button(
                            onClick = { onIncrementStock(product) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("restock_button_${product.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = addLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}
