package id.biz.majulancar.kasir.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.biz.majulancar.kasir.data.Catalog
import id.biz.majulancar.kasir.data.MenuItem
import id.biz.majulancar.kasir.ui.AppViewModel
import id.biz.majulancar.kasir.ui.components.ScreenTitle
import id.biz.majulancar.kasir.ui.components.StepperButton
import id.biz.majulancar.kasir.ui.rupiah

private data class CartRow(val item: MenuItem, val quantity: Int)

@Composable
fun CartScreen(vm: AppViewModel, onGoToMenu: () -> Unit) {
    val cart by vm.cart.collectAsStateWithLifecycle()
    val rows = remember(cart) {
        cart.mapNotNull { line -> Catalog.find(line.itemId)?.let { CartRow(it, line.quantity) } }
    }
    val total: Long = rows.sumOf { it.item.price * it.quantity }

    var showPay by rememberSaveable { mutableStateOf(false) }
    var showClearConfirm by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenTitle("🛒 Keranjang", subtitle = if (rows.isEmpty()) null else "${rows.sumOf { it.quantity }} item")

        if (rows.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("🛒", style = MaterialTheme.typography.displayMedium)
                Text("Keranjang Kosong", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "Yuk, pilih menu favorit pelanggan!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                Button(onClick = onGoToMenu) { Text("Lihat Menu") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(rows, key = { it.item.id }) { row ->
                    CartRowCard(
                        row = row,
                        onMinus = { vm.changeQty(row.item.id, -1) },
                        onPlus = { vm.changeQty(row.item.id, +1) },
                        onRemove = { vm.removeLine(row.item.id) },
                    )
                }
            }

            Card(
                modifier = Modifier.padding(vertical = 12.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(
                            total.rupiah(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showClearConfirm = true }, modifier = Modifier.weight(1f)) {
                            Text("Kosongkan")
                        }
                        Button(onClick = { showPay = true }, modifier = Modifier.weight(1f)) { Text("💳 Bayar") }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Kosongkan keranjang?") },
            text = { Text("Semua item di keranjang akan dihapus.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    vm.clearCart()
                }) { Text("Kosongkan") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Batal") } },
        )
    }

    if (showPay && total > 0) {
        PaymentDialog(
            total = total,
            saving = saving,
            onDismiss = { showPay = false },
            onConfirm = { method, cash ->
                saving = true
                vm.checkout(method, cash) { ok ->
                    saving = false
                    if (ok) showPay = false
                }
            },
        )
    }
}

@Composable
private fun CartRowCard(row: CartRow, onMinus: () -> Unit, onPlus: () -> Unit, onRemove: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${row.quantity} x ${row.item.price.rupiah()} = ${(row.item.price * row.quantity).rupiah()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StepperButton("−", "Kurangi jumlah ${row.item.name}", onMinus)
            Text(
                row.quantity.toString(),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
            )
            StepperButton("+", "Tambah jumlah ${row.item.name}", onPlus)
            TextButton(onClick = onRemove) { Text("🗑") }
        }
    }
}
