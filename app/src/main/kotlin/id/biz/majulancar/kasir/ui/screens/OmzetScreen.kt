package id.biz.majulancar.kasir.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.biz.majulancar.kasir.data.Limits
import id.biz.majulancar.kasir.data.PaymentMethod
import id.biz.majulancar.kasir.data.Sale
import id.biz.majulancar.kasir.data.SalesSummary
import id.biz.majulancar.kasir.ui.AppViewModel
import id.biz.majulancar.kasir.ui.components.ScreenTitle
import id.biz.majulancar.kasir.ui.formatWaktu
import id.biz.majulancar.kasir.ui.rupiah

private data class Stat(val label: String, val value: String)

@Composable
fun OmzetScreen(vm: AppViewModel) {
    val sales by vm.sales.collectAsStateWithLifecycle()
    val summary = remember(sales) { SalesSummary.of(sales) }
    val history = remember(sales) { sales.asReversed().take(Limits.MAX_HISTORY_SHOWN) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    val stats = listOf(
        Stat("💰 Total Omzet", summary.totalOmzet.rupiah()),
        Stat("📅 Omzet Hari Ini", summary.omzetHariIni.rupiah()),
        Stat("🧾 Total Transaksi", "${summary.totalTransaksi}x"),
        Stat("🧾 Transaksi Hari Ini", "${summary.transaksiHariIni}x"),
        Stat("📱 Omzet QRIS", "${summary.omzetQris.rupiah()} (${summary.transaksiQris}x)"),
        Stat("💵 Omzet Tunai", "${summary.omzetTunai.rupiah()} (${summary.transaksiTunai}x)"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "title") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScreenTitle("📊 Omzet Penjualan", modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { confirmReset = true }, enabled = sales.isNotEmpty()) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        items(stats.chunked(2), key = { it.first().label }) { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { StatCard(it, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item(key = "history_title") {
            Text(
                "Riwayat Transaksi Terbaru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (history.isEmpty()) {
            item(key = "empty") {
                Text(
                    "Belum ada transaksi yang tercatat.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(history, key = { it.id }) { SaleCard(it) }
        }
        item(key = "note") {
            Text(
                "🔒 Data omzet disimpan terenkripsi (AES-256) hanya di perangkat ini dan tidak dikirim ke mana pun.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset seluruh omzet?") },
            text = {
                Text(
                    "${summary.totalTransaksi} transaksi (${summary.totalOmzet.rupiah()}) akan dihapus permanen " +
                        "dari perangkat ini. Tindakan ini tidak bisa dibatalkan."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    vm.resetSales()
                }) { Text("Ya, hapus semua", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun StatCard(stat: Stat, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stat.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stat.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SaleCard(sale: Sale) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        sale.id,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatWaktu(sale.createdAtMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(
                            if (sale.method == PaymentMethod.CASH) "💵 Tunai" else "📱 QRIS",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    sale.total.rupiah(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            sale.items.forEach { item ->
                Text(
                    "${item.quantity} x ${item.price.rupiah()} (${item.name}) = ${item.subtotal.rupiah()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (sale.method == PaymentMethod.CASH && sale.cashReceived != null && sale.change != null) {
                Text(
                    "Uang: ${sale.cashReceived.rupiah()}   Kembali: ${sale.change.rupiah()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
