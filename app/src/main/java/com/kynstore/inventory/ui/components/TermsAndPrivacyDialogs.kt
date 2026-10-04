package com.kynstore.inventory.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynstore.inventory.ui.theme.BlueContainer
import com.kynstore.inventory.ui.theme.BlueDark
import com.kynstore.inventory.ui.theme.BluePrimary
import com.kynstore.inventory.ui.theme.TextDark
import com.kynstore.inventory.ui.theme.TextMuted
import com.kynstore.inventory.ui.theme.TextSubtle
import com.kynstore.inventory.util.AppLanguage
import com.kynstore.inventory.util.rememberAppLanguage

/**
 * Terms and Conditions Dialog
 * Strictly explains offline operation, local on-device persistence, and safe updates.
 */
@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit
) {
    val lang = rememberAppLanguage()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BlueContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    BoxContent(icon = Icons.Default.Gavel, color = BlueDark)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.TAGALOG -> "Mga Tuntunin at Kundisyon"
                            AppLanguage.ENGLISH -> "Terms & Conditions"
                            AppLanguage.BISAYA -> "Mga Lagda ug Kondisyon"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "100% Offline App • On-Device Storage",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "1. 100% On-Device at Walang Cloud"
                        AppLanguage.ENGLISH -> "1. 100% On-Device and Zero Cloud"
                        AppLanguage.BISAYA -> "1. 100% On-Device ug Walay Cloud"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Ang KYN Store ay eksklusibong gumagana nang walang cloud o panlabas na server. Ang lahat ng listahan ng produkto, barcode, dami ng stock, at mga presyo ay ligtas na nakaimbak lamang sa internal private storage ng iyong mismong device."
                        AppLanguage.ENGLISH -> "KYN Store operates exclusively without cloud dependencies or external servers. All product listings, barcodes, stock levels, and prices are securely stored only within your device's internal private storage."
                        AppLanguage.BISAYA -> "Ang KYN Store naglihok nga walay cloud o external server. Ang tanang listahan sa produkto, barcode, gidaghanon sa stock, ug mga presyo luwas nga nakatago lamang sa internal storage sa imong device."
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "2. Proteksyon sa Bawat Application Update"
                        AppLanguage.ENGLISH -> "2. Protection Across App Updates"
                        AppLanguage.BISAYA -> "2. Proteksyon sa Matag Update sa App"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Ginagarantiya ng application ang non-destructive database persistence. Tuwing magkakaroon ng pag-update ang app, ang lahat ng iyong umiiral na mga tala, naitalang benta, at listahan ng imbentaryo ay protektado at HINDI mabubura o mawawala."
                        AppLanguage.ENGLISH -> "The application guarantees non-destructive database persistence. Whenever the app is updated, all your existing records, recorded sales, and inventory counts are preserved and will NEVER be erased."
                        AppLanguage.BISAYA -> "Gipasalig sa aplikasyon ang non-destructive persistence. Sa matag update sa app, ang tanan nimong mga rekord, halin, ug imbentaryo protektado ug DILI gayud mapapas o mawala."
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "3. Proteksyon Laban sa Data Leakage"
                        AppLanguage.ENGLISH -> "3. Protection Against Data Leakage"
                        AppLanguage.BISAYA -> "3. Proteksyon Batok sa Pag-leak sa Data"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Dahil walang koneksyon sa internet o external backend API, protektado ang iyong tindahan mula sa data breaches at remote leaks. Ang mga sensitibong detalye at internal database identifiers ay nakatago at hindi inilalantad sa publiko."
                        AppLanguage.ENGLISH -> "Because there is zero internet connectivity or backend API, your store is immune to remote data breaches and leaks. Sensitive details and database internals remain completely hidden."
                        AppLanguage.BISAYA -> "Tungod kay walay koneksyon sa internet o external server, protektado ang imong tindahan gikan sa remote data breaches. Ang mga sensitibong detalye pabilin nga tago."
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "4. Lokal na Backup at Pagmamay-ari"
                        AppLanguage.ENGLISH -> "4. Local Backup and Complete Ownership"
                        AppLanguage.BISAYA -> "4. Lokal nga Backup ug Pagpanag-iya"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Ikaw ang may ganap na pagmamay-ari sa iyong datos. Maaari kang mag-export ng CSV o JSON backup nang direkta sa storage ng iyong telepono anumang oras para sa sarili mong backup copies."
                        AppLanguage.ENGLISH -> "You retain full, exclusive ownership of your data. You may export CSV or JSON backups directly to your device storage at any time for your personal backup archives."
                        AppLanguage.BISAYA -> "Ikaw ang tag-iya sa tanan nimong datos. Mahimo kang mag-export og CSV o JSON backup direkta sa storage sa imong telepono bisan unsang orasa."
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier.testTag("close_terms_dialog_button")
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.TAGALOG -> "Naiintindihan Ko"
                        AppLanguage.ENGLISH -> "I Understand"
                        AppLanguage.BISAYA -> "Nakasabot Ko"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

/**
 * Privacy Policy Dialog
 * Explains zero-cloud data collection, sandbox security, and hardware sensor handling.
 */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val lang = rememberAppLanguage()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BlueContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    BoxContent(icon = Icons.Default.PrivacyTip, color = BlueDark)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.TAGALOG -> "Patakaran sa Privacy"
                            AppLanguage.ENGLISH -> "Privacy Policy"
                            AppLanguage.BISAYA -> "Patakaran sa Privacy"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "100% Offline • Zero Telemetry",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "1. Zero Data Harvesting at Walang Tracking"
                        AppLanguage.ENGLISH -> "1. Zero Data Harvesting and No Tracking"
                        AppLanguage.BISAYA -> "1. Zero Data Harvesting ug Walay Tracking"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Hindi kami nangongolekta, nagpapadala, o nagbabahagi ng anumang personal na impormasyon, pangalan ng may-ari, mga benta, o mga presyo. Walang naka-install na trackers, walang analytics probes, at walang advertising networks."
                        AppLanguage.ENGLISH -> "We do not collect, transmit, or share any personal information, owner names, sales transactions, or prices. Zero trackers, zero analytics probes, and zero advertising networks are installed."
                        AppLanguage.BISAYA -> "Wala kami nagkolekta, nagpadala, o nag-ambit og bisan unsang personal nga impormasyon, ngalan sa tag-iya, halin, o presyo. Walay trackers ug walay analytics."
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "2. Paggamit ng Camera Sensor"
                        AppLanguage.ENGLISH -> "2. Camera Sensor Usage"
                        AppLanguage.BISAYA -> "2. Paggamit sa Camera Sensor"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Ang pahintulot sa Camera ay ginagamit LAMANG sa on-device barcode scanning sa pamamagitan ng ML Kit barcode reader. Walang larawan o bidyo na nire-record o ipinapadala sa labas."
                        AppLanguage.ENGLISH -> "Camera permission is used EXCLUSIVELY for on-device barcode scanning via ML Kit. No photos or video streams are ever recorded, saved to gallery, or transmitted outside."
                        AppLanguage.BISAYA -> "Ang pagtugot sa Camera gigamit LAMANG sa on-device barcode scanning pinaagi sa ML Kit. Walay hulagway o video nga i-rekord o ipadala sa gawas."
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "3. Android Sandbox at Storage Security"
                        AppLanguage.ENGLISH -> "3. Android Sandbox and Storage Security"
                        AppLanguage.BISAYA -> "3. Android Sandbox ug Kaluwasan sa Storage"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Ang database ay nananatili sa loob ng Android Application Sandbox na protektado ng operating system. Ang mga backup files na iyong i-e-export ay direktang inililipat lamang sa folder na iyong pinili."
                        AppLanguage.ENGLISH -> "The database resides strictly inside the Android Application Sandbox protected by the OS kernel. Exported backup files are saved directly to your chosen local directory."
                        AppLanguage.BISAYA -> "Ang database pabilin sa sulod sa Android Application Sandbox nga gipanalipdan sa operating system. Ang mga backup files maadto lamang sa folder nga imong gipili."
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    when (lang) {
                        AppLanguage.TAGALOG -> "4. Pagbura ng Datos"
                        AppLanguage.ENGLISH -> "4. Data Deletion"
                        AppLanguage.BISAYA -> "4. Pagpapas sa Datos"
                    }
                )
                BodyParagraph(
                    when (lang) {
                        AppLanguage.TAGALOG -> "Kung nais mong burahin ang lahat ng datos, maaari mong tanggalin ang mga produkto sa loob ng app o i-clear ang app storage sa settings ng iyong device."
                        AppLanguage.ENGLISH -> "If you wish to delete all data, you can delete items directly inside the app or clear app storage in your Android device system settings."
                        AppLanguage.BISAYA -> "Kung gusto nimong papason ang tanang datos, mahimo nimong tangtangon ang mga produkto sa app o i-clear ang app storage sa settings sa imong device."
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier.testTag("close_privacy_dialog_button")
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.TAGALOG -> "Sumasang-ayon Ako"
                        AppLanguage.ENGLISH -> "I Agree"
                        AppLanguage.BISAYA -> "Miuyon Ako"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun BoxContent(icon: ImageVector, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = BlueDark
    )
}

@Composable
private fun BodyParagraph(text: String) {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TextDark,
        lineHeight = 20.sp
    )
}
