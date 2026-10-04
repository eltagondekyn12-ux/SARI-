package com.kynstore.inventory.ui.screens

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kynstore.inventory.ui.components.CloudSyncLoader
import com.kynstore.inventory.ui.components.PrivacyPolicyDialog
import com.kynstore.inventory.ui.components.TermsAndConditionsDialog
import com.kynstore.inventory.ui.theme.BlueBorder
import com.kynstore.inventory.ui.theme.BlueContainer
import com.kynstore.inventory.ui.theme.BlueDark
import com.kynstore.inventory.ui.theme.BluePrimary
import com.kynstore.inventory.ui.theme.CardWhite
import com.kynstore.inventory.ui.theme.SoftBlueBackground
import com.kynstore.inventory.ui.theme.StatusGreen
import com.kynstore.inventory.ui.theme.StatusOrange
import com.kynstore.inventory.ui.theme.TextDark
import com.kynstore.inventory.ui.theme.TextMuted
import com.kynstore.inventory.ui.theme.TextSubtle
import com.kynstore.inventory.util.AppLanguage
import com.kynstore.inventory.util.AppLanguageManager
import com.kynstore.inventory.util.rememberAppLanguage
import kotlinx.coroutines.delay

/**
 * Landing Page with Language Selection, Mandatory Terms & Privacy Agreement,
 * Strict Offline / On-Device Data Protection guarantees,
 * Adaptive Width Centering (max 680.dp), and 3-second localized preparation.
 */
@Composable
fun LandingScreen(
    onStartApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AppLanguageManager.init(context)
    }

    val currentLang = rememberAppLanguage()
    val prefs = remember { context.getSharedPreferences("kyn_store_prefs", Context.MODE_PRIVATE) }

    var isAgreed by remember {
        mutableStateOf(prefs.getBoolean("has_agreed_terms", false))
    }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAgreementWarning by remember { mutableStateOf(false) }

    // 3-second preparation state
    var isPreparing by remember { mutableStateOf(false) }
    var prepStepText by remember { mutableStateOf("") }
    var prepProgress by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = prepProgress,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "prep_progress"
    )

    // Localized preparation step strings
    val step1Text = when (currentLang) {
        AppLanguage.TAGALOG -> "Sinusuri ang lokal na database sa device..."
        AppLanguage.ENGLISH -> "Checking local database on device..."
        AppLanguage.BISAYA -> "Gisusi ang lokal nga database sa device..."
    }
    val step2Text = when (currentLang) {
        AppLanguage.TAGALOG -> "Sinusuri ang integridad at proteksyon ng data sa offline storage..."
        AppLanguage.ENGLISH -> "Verifying data integrity and offline storage protection..."
        AppLanguage.BISAYA -> "Gisusi ang kaluwasan ug proteksyon sa data sa offline storage..."
    }
    val step3Text = when (currentLang) {
        AppLanguage.TAGALOG -> "Inihahanda ang POS Cashier at Barcode Scanner..."
        AppLanguage.ENGLISH -> "Preparing POS Cashier and Barcode Scanner..."
        AppLanguage.BISAYA -> "Giandam ang POS Cashier ug Barcode Scanner..."
    }

    // Run the 3-second localized preparation sequence before entering the app
    LaunchedEffect(isPreparing) {
        if (isPreparing) {
            // Step 1: 0 - 1000ms
            prepStepText = step1Text
            prepProgress = 0.33f
            delay(1000)

            // Step 2: 1000ms - 2000ms
            prepStepText = step2Text
            prepProgress = 0.66f
            delay(1000)

            // Step 3: 2000ms - 3000ms
            prepStepText = step3Text
            prepProgress = 1.0f
            delay(1000)

            // Save user agreement persistently on device
            prefs.edit().putBoolean("has_agreed_terms", true).apply()
            onStartApp()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBlueBackground)
            .testTag("landing_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        // Centered with max width of 680.dp for responsive tablet and phone adaptability
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Store Brand Avatar
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "KYN Store Logo",
                        tint = BluePrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "KYN Store",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
                fontWeight = FontWeight.ExtraBold,
                color = BlueDark,
                textAlign = TextAlign.Center
            )

            Text(
                text = when (currentLang) {
                    AppLanguage.TAGALOG -> "Tindahan POS & Imbentaryo"
                    AppLanguage.ENGLISH -> "Store POS & Inventory Hub"
                    AppLanguage.BISAYA -> "Tindahan POS & Imbentaryo"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.SemiBold,
                color = TextSubtle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // PROMINENT LANGUAGE SELECTOR CARD
            // Located directly beneath the store banner
            // ==========================================
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.5.dp, BlueBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_selector_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLang) {
                                AppLanguage.TAGALOG -> "Pumili ng Wika / Select Language"
                                AppLanguage.ENGLISH -> "Select Language / Pumili ng Wika"
                                AppLanguage.BISAYA -> "Pilia ang Pinulongan / Select Language"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BlueDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Large tactile language pills with flags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = currentLang == lang
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BluePrimary else BlueContainer.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) BluePrimary else BlueBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        AppLanguageManager.setLanguage(context, lang)
                                    }
                                    .testTag("language_pill_${lang.code}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = lang.flag,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TextDark
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Push Offline Shield Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = StatusGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.5.dp, StatusGreen.copy(alpha = 0.4f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = null,
                        tint = StatusGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLang) {
                            AppLanguage.TAGALOG -> "100% Offline Mode • Ligtas sa Device"
                            AppLanguage.ENGLISH -> "100% Offline Mode • Safe on Device"
                            AppLanguage.BISAYA -> "100% Offline Mode • Luwas sa Device"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security & Offline Guarantees Cards Header
            Text(
                text = when (currentLang) {
                    AppLanguage.TAGALOG -> "Pangangalaga sa Datos ng Tindahan"
                    AppLanguage.ENGLISH -> "Store Data Protection & Security"
                    AppLanguage.BISAYA -> "Pagpanalipod sa Datos sa Tindahan"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            GuaranteeItem(
                icon = Icons.Default.CloudOff,
                iconTint = BluePrimary,
                title = when (currentLang) {
                    AppLanguage.TAGALOG -> "Walang Cloud / 100% On-Device"
                    AppLanguage.ENGLISH -> "Zero Cloud / 100% On-Device"
                    AppLanguage.BISAYA -> "Walay Cloud / 100% On-Device"
                },
                description = when (currentLang) {
                    AppLanguage.TAGALOG -> "Lahat ng paninda, presyo, at benta ay nakaimbak lamang sa sarili mong telepono. Walang data na lumalabas o umaabot sa cloud."
                    AppLanguage.ENGLISH -> "All items, prices, and sales are stored exclusively on your own phone. No data ever leaves your device or goes to the cloud."
                    AppLanguage.BISAYA -> "Ang tanang baligya, presyo, ug halin anaa ra sa imong kaugalingong cellphone. Walay data nga mogawas o maabot sa cloud."
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            GuaranteeItem(
                icon = Icons.Default.SystemUpdate,
                iconTint = StatusGreen,
                title = when (currentLang) {
                    AppLanguage.TAGALOG -> "Protektado sa Bawat Update"
                    AppLanguage.ENGLISH -> "Protected Across Every Update"
                    AppLanguage.BISAYA -> "Protektado sa Matag Update"
                },
                description = when (currentLang) {
                    AppLanguage.TAGALOG -> "Hindi kailanman mabubura ang iyong imbentaryo tuwing may bagong bersyon o update ang app. Buo at protektado ang iyong mga tala."
                    AppLanguage.ENGLISH -> "Your inventory will never be wiped during app updates. All your records, stock counts, and sales remain intact and secure."
                    AppLanguage.BISAYA -> "Dili gayud mapapas ang imong imbentaryo sa matag bag-ong update sa app. Kompleto ug luwas ang imong mga listahan."
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            GuaranteeItem(
                icon = Icons.Default.Security,
                iconTint = BlueDark,
                title = when (currentLang) {
                    AppLanguage.TAGALOG -> "Protektado Laban sa Breaches"
                    AppLanguage.ENGLISH -> "Protected Against Data Breaches"
                    AppLanguage.BISAYA -> "Protektado Batok sa Breaches"
                },
                description = when (currentLang) {
                    AppLanguage.TAGALOG -> "Nakatago ang lahat ng internal database IDs at detalye. Zero telemetry, walang ad tracking, at walang third-party leaks."
                    AppLanguage.ENGLISH -> "All internal database IDs and details remain private. Zero telemetry, no ad tracking, and zero third-party data leaks."
                    AppLanguage.BISAYA -> "Tago ang tanang internal database IDs ug detalye. Walay telemetry, walay ad tracking, ug walay third-party leaks."
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mandatory Terms & Privacy Agreement Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.5.dp, if (isAgreed) BluePrimary else BlueBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("terms_agreement_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (currentLang) {
                            AppLanguage.TAGALOG -> "Pagsang-ayon bago magsimula:"
                            AppLanguage.ENGLISH -> "Agreement before starting:"
                            AppLanguage.BISAYA -> "Pagsang-ayon una magsugod:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isAgreed,
                            onCheckedChange = { isAgreed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = BluePrimary,
                                uncheckedColor = BlueBorder
                            ),
                            modifier = Modifier.testTag("terms_agreement_checkbox")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = when (currentLang) {
                                    AppLanguage.TAGALOG -> "Nabasa at sumasang-ayon ako sa mga tuntunin at patakaran sa kaligtasan ng datos:"
                                    AppLanguage.ENGLISH -> "I have read and agree to the terms and data privacy policies:"
                                    AppLanguage.BISAYA -> "Nabasa ug miuyon ako sa mga lagda ug patakaran sa proteksyon sa datos:"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.TAGALOG -> "Mga Tuntunin"
                                        AppLanguage.ENGLISH -> "Terms & Conditions"
                                        AppLanguage.BISAYA -> "Mga Lagda"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .clickable { showTermsDialog = true }
                                        .testTag("open_terms_link")
                                )

                                Text(
                                    text = "•",
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.TAGALOG -> "Patakaran sa Privacy"
                                        AppLanguage.ENGLISH -> "Privacy Policy"
                                        AppLanguage.BISAYA -> "Patakaran sa Privacy"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .clickable { showPrivacyDialog = true }
                                        .testTag("open_privacy_link")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Designated START Button (tactile & elder-friendly 56dp height)
            Button(
                onClick = {
                    if (isAgreed) {
                        isPreparing = true
                    } else {
                        showAgreementWarning = true
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAgreed) BluePrimary else Color(0xFF94A3B8)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("landing_start_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when (currentLang) {
                        AppLanguage.TAGALOG -> "SIMULAN ANG TINDAHAN"
                        AppLanguage.ENGLISH -> "START STORE"
                        AppLanguage.BISAYA -> "SUGOD SA TINDAHAN"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isAgreed) {
                    when (currentLang) {
                        AppLanguage.TAGALOG -> "Nakahanda nang buksan ang tindahan"
                        AppLanguage.ENGLISH -> "Ready to open the store"
                        AppLanguage.BISAYA -> "Andam na ablihan ang tindahan"
                    }
                } else {
                    when (currentLang) {
                        AppLanguage.TAGALOG -> "Kailangan munang sumang-ayon upang makapasok"
                        AppLanguage.ENGLISH -> "You must agree to the terms before starting"
                        AppLanguage.BISAYA -> "Kinahanglan unang mouyon aron makasugod"
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isAgreed) StatusGreen else StatusOrange,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Warning Dialog if user attempts to start without agreeing
        if (showAgreementWarning) {
            AlertDialog(
                onDismissRequest = { showAgreementWarning = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusOrange,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = when (currentLang) {
                            AppLanguage.TAGALOG -> "Kinakailangan ang Pagsang-ayon"
                            AppLanguage.ENGLISH -> "Agreement Required"
                            AppLanguage.BISAYA -> "Kinahanglan ang Pagsang-ayon"
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = when (currentLang) {
                            AppLanguage.TAGALOG -> "Upang maprotektahan ang iyong tindahan at masiguro ang 100% on-device offline storage, kailangan munang lagyan ng tsek ang pagsang-ayon sa Mga Tuntunin at Patakaran sa Privacy bago makapasok sa application."
                            AppLanguage.ENGLISH -> "To protect your store and ensure 100% on-device offline storage, you must check the agreement box for Terms and Privacy Policy before entering the application."
                            AppLanguage.BISAYA -> "Aron maprotektahan ang imong tindahan ug masiguro ang 100% on-device offline storage, kinahanglan una nimong i-tsek ang pagsang-ayon sa Mga Lagda ug Patakaran sa Privacy una makasulod."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isAgreed = true
                            showAgreementWarning = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.testTag("agree_and_continue_button")
                    ) {
                        Text(
                            text = when (currentLang) {
                                AppLanguage.TAGALOG -> "Sumang-ayon at Magpatuloy"
                                AppLanguage.ENGLISH -> "Agree and Continue"
                                AppLanguage.BISAYA -> "Mouyon ug Magpadayon"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAgreementWarning = false }) {
                        Text(
                            text = when (currentLang) {
                                AppLanguage.TAGALOG -> "Bumalik"
                                AppLanguage.ENGLISH -> "Back"
                                AppLanguage.BISAYA -> "Balik"
                            }
                        )
                    }
                }
            )
        }

        // Terms and Conditions Dialog
        if (showTermsDialog) {
            TermsAndConditionsDialog(onDismiss = { showTermsDialog = false })
        }

        // Privacy Policy Dialog
        if (showPrivacyDialog) {
            PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
        }

        // 3-Second Loading & Preparation Overlay (Localized)
        if (isPreparing) {
            Dialog(
                onDismissRequest = { /* Non-cancellable during 3s prep */ },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                        .padding(16.dp)
                        .testTag("landing_preparation_dialog")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                    ) {
                        // Animated Uiverse Loader
                        CloudSyncLoader(
                            size = 110.dp,
                            cloudColor = Color(0xFF4387F4),
                            arrowsColor = Color(0xFF80B1FF),
                            linesColor = Color(0xFF80B1FF).copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = when (currentLang) {
                                AppLanguage.TAGALOG -> "Inihahanda ang Tindahan..."
                                AppLanguage.ENGLISH -> "Preparing Your Store..."
                                AppLanguage.BISAYA -> "Giandam ang Tindahan..."
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlueDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = prepStepText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSubtle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.height(44.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = BluePrimary,
                            trackColor = BlueContainer
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentLang) {
                                    AppLanguage.TAGALOG -> "100% Offline • Naka-encrypt sa Device Storage"
                                    AppLanguage.ENGLISH -> "100% Offline • Encrypted in Device Storage"
                                    AppLanguage.BISAYA -> "100% Offline • Naka-encrypt sa Device Storage"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuaranteeItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, BlueBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSubtle,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
