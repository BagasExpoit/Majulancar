package id.biz.majulancar.kasir.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.biz.majulancar.kasir.data.Catalog
import id.biz.majulancar.kasir.data.MenuCategory
import id.biz.majulancar.kasir.data.MenuItem
import id.biz.majulancar.kasir.data.StoreInfo
import id.biz.majulancar.kasir.ui.AppViewModel
import id.biz.majulancar.kasir.ui.components.StepperButton
import id.biz.majulancar.kasir.ui.components.StorePills
import id.biz.majulancar.kasir.ui.rupiah
import id.biz.majulancar.kasir.ui.theme.Chili
import id.biz.majulancar.kasir.ui.theme.ChiliDeep

private const val ALL = "semua"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(vm: AppViewModel, onOpenCart: () -> Unit) {
    val cart by vm.cart.collectAsStateWithLifecycle()
    val pending by vm.pending.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableStateOf(ALL) }

    val visible = if (selected == ALL) Catalog.categories else Catalog.categories.filter { it.key == selected }
    val cartCount = cart.sumOf { it.quantity }
    val cartTotal: Long = cart.sumOf { line -> (Catalog.find(line.itemId)?.price ?: 0L) * line.quantity }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = if (cartCount > 0) 104.dp else 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "hero") { Hero() }
            item(key = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selected == ALL,
                            onClick = { selected = ALL },
                            label = { Text("Semua") },
                        )
                    }
                    items(Catalog.categories, key = { it.key }) { cat ->
                        FilterChip(
                            selected = selected == cat.key,
                            onClick = { selected = cat.key },
                            label = { Text("${cat.emoji} ${cat.label.removePrefix("Menu ")}") },
                        )
                    }
                }
            }
            visible.forEach { cat ->
                item(key = "header_${cat.key}") { CategoryHeader(cat) }
                items(cat.items, key = { it.id }) { menuItem ->
                    MenuItemCard(
                        item = menuItem,
                        emoji = cat.emoji,
                        quantity = pending[menuItem.id] ?: 1,
                        onMinus = { vm.changePending(menuItem.id, -1) },
                        onPlus = { vm.changePending(menuItem.id, +1) },
                        onAdd = { vm.addToCart(menuItem) },
                    )
                }
            }
        }

        if (cartCount > 0) {
            Surface(
                onClick = onOpenCart,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("🛒 $cartCount item", style = MaterialTheme.typography.labelLarge)
                        Text(cartTotal.rupiah(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("Lihat Keranjang  ›", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun Hero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Chili, ChiliDeep)),
                RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("KASIR", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
            Text(
                "🍜 Mie Ayam Maju Lancar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "📍 ${StoreInfo.ADDRESS}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
            )
            StorePills(onDark = true)
        }
    }
}

@Composable
private fun CategoryHeader(cat: MenuCategory) {
    Text(
        "${cat.emoji}  ${cat.label}",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    emoji: String,
    quantity: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onAdd: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        item.price.rupiah(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepperButton("−", "Kurangi jumlah ${item.name}", onMinus)
                Text(
                    quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(40.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                StepperButton("+", "Tambah jumlah ${item.name}", onPlus)
                Spacer(Modifier.width(12.dp))
                Button(onClick = onAdd, modifier = Modifier.weight(1f)) { Text("🛒 Tambah") }
            }
        }
    }
}
