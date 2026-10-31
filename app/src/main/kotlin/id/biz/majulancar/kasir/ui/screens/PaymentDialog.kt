package id.biz.majulancar.kasir.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import id.biz.majulancar.kasir.core.Qris
import id.biz.majulancar.kasir.data.Limits
import id.biz.majulancar.kasir.data.PaymentMethod
import id.biz.majulancar.kasir.data.StoreInfo
import id.biz.majulancar.kasir.ui.components.QrCodeImage
import id.biz.majulancar.kasir.ui.quickCashAmounts
import id.biz.majulancar.kasir.ui.rupiah

@Composable
fun PaymentDialog(
    total: Long,
    saving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod, Long?) -> Unit,
) {
    var method by rememberSaveable { mutableStateOf(PaymentMethod.QRIS) }
    var cashInput by rememberSaveable { mutableStateOf("") }

    val qris = remember(total) { Qris.generateDynamicSafe(StoreInfo.QRIS_STATIC_PAYLOAD, total) }
    val cashReceived = cashInput.toLongOrNull() ?: 0L
    val change = cashReceived - total
    val cashSufficient = cashReceived >= total
    val canFinish = !saving && (method == PaymentMethod.QRIS || cashSufficient)

    Dialog(
        onDismissRequest = { if (!saving) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !saving,
            dismissOnClickOutside = false,
            // Jendela dialog terpisah dari Activity: QR/total ikut dilindungi dari screenshot & perekam layar.
            securePolicy = SecureFlagPolicy.SecureOn,
        ),
    ) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("💰 Pilih Cara Bayar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MethodButton("QRIS", method == PaymentMethod.QRIS, !saving, Modifier.weight(1f)) {
                        method = PaymentMethod.QRIS
                    }
                    MethodButton("Tunai", method == PaymentMethod.CASH, !saving, Modifier.weight(1f)) {
                        method = PaymentMethod.CASH
                    }
                }

                if (method == PaymentMethod.QRIS) {
                    Text(
                        "Scan QR Code di bawah untuk membayar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    QrCodeImage(qris.payload, Modifier.size(248.dp))
                    if (qris.isDynamic) {
                        Text(
                            "Nominal sudah sesuai total pesanan — tinggal scan & bayar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(
                            "QRIS dinamis gagal dibuat; pembeli harus mengetik nominal ${total.rupiah()} secara manual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { raw ->
                            cashInput = raw.filter { it in '0'..'9' }.take(Limits.MAX_CASH_DIGITS)
                        },
                        label = { Text("Uang diterima dari pembeli") },
                        prefix = { Text("Rp ") },
                        singleLine = true,
                        enabled = !saving,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        supportingText = { if (cashInput.isNotEmpty()) Text(cashReceived.rupiah()) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        quickCashAmounts(total).forEach { amount ->
                            OutlinedButton(onClick = { cashInput = amount.toString() }, enabled = !saving) {
                                Text(if (amount == total) "Uang Pas" else amount.rupiah())
                            }
                        }
                    }
                    Surface(
                        color = if (cashSufficient) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Kembalian", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                                Text(
                                    if (cashInput.isEmpty()) "—" else maxOf(change, 0L).rupiah(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if (cashInput.isNotEmpty() && !cashSufficient) {
                                Text(
                                    "Uang kurang ${(total - cashReceived).rupiah()}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()
                Text("Total Belanja", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    total.rupiah(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(StoreInfo.NAME, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, enabled = !saving, modifier = Modifier.weight(1f)) { Text("Tutup") }
                    Button(
                        onClick = { onConfirm(method, if (method == PaymentMethod.CASH) cashReceived else null) },
                        enabled = canFinish,
                        modifier = Modifier.weight(1f),
                    ) { Text(if (saving) "Menyimpan..." else "✓ Selesai") }
                }
            }
        }
    }
}

@Composable
private fun MethodButton(label: String, selected: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, enabled = enabled, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier) { Text(label) }
    }
}
