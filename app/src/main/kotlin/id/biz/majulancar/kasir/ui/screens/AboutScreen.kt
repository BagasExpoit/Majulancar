package id.biz.majulancar.kasir.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.biz.majulancar.kasir.BuildConfig
import id.biz.majulancar.kasir.data.StoreInfo
import id.biz.majulancar.kasir.ui.components.FacebookIcon
import id.biz.majulancar.kasir.ui.components.InstagramIcon
import id.biz.majulancar.kasir.ui.components.StorePills
import id.biz.majulancar.kasir.ui.components.openLink

@Composable
fun AboutScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍜", style = MaterialTheme.typography.displayMedium)
            Text(
                "Mie Ayam Maju Lancar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text("MAS TRI", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StorePills(modifier = Modifier.padding(top = 12.dp))
        }

        InfoCard("📍 Lokasi") {
            Text(StoreInfo.ADDRESS, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { openLink(context, StoreInfo.MAPS_URL) }, modifier = Modifier.fillMaxWidth()) {
                Text("Buka di Maps")
            }
        }

        InfoCard("📞 Kontak") {
            Button(onClick = { openLink(context, StoreInfo.WHATSAPP_URL) }, modifier = Modifier.fillMaxWidth()) {
                Text("WhatsApp · ${StoreInfo.PHONE}")
            }
            OutlinedButton(onClick = { openLink(context, StoreInfo.ADMIN_CONTACT_URL) }, modifier = Modifier.fillMaxWidth()) {
                Text("ADMIN (Telegram)")
            }
        }

        InfoCard("👨‍💻 Developer") {
            OutlinedButton(onClick = { openLink(context, "https://facebook.com/willydevops1") }, modifier = Modifier.fillMaxWidth()) {
                FacebookIcon(Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text("bagas orcarius")
            }
            OutlinedButton(
                onClick = { openLink(context, "https://www.instagram.com/bagas_samuji74?igsh=bmlscWxqN2pmZnJx") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                InstagramIcon(Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text("bagas_samuji74")
            }
        }

        Text(
            "Versi ${BuildConfig.VERSION_NAME} · © Mie Ayam Maju Lancar Mas Tri",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}
