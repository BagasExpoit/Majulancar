package id.biz.majulancar.kasir.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.biz.majulancar.kasir.core.SafeLinks
import id.biz.majulancar.kasir.data.StoreInfo
import id.biz.majulancar.kasir.ui.isOpenNow
import id.biz.majulancar.kasir.ui.theme.Basil
import id.biz.majulancar.kasir.ui.theme.Chili
import kotlinx.coroutines.delay

/** Buka tautan HANYA bila lolos allowlist [SafeLinks]. */
fun openLink(context: Context, url: String) {
    if (!SafeLinks.isAllowed(url)) {
        Toast.makeText(context, "Tautan diblokir demi keamanan.", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Tidak ada aplikasi untuk membuka tautan ini.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ScreenTitle(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StepperButton(label: String, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp).semantics { contentDescription = description },
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun rememberIsOpen(): State<Boolean> = produceState(initialValue = isOpenNow()) {
    while (true) {
        delay(60_000)
        value = isOpenNow()
    }
}

/** Rating, jam buka + status Buka/Tutup real-time. [onDark] = tampil di atas latar berwarna. */
@Composable
fun StorePills(modifier: Modifier = Modifier, onDark: Boolean = false) {
    val isOpen by rememberIsOpen()
    val bg = if (onDark) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (onDark) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Pill(bg) {
            Text(
                "⭐ ${"%.1f".format(StoreInfo.RATING_VALUE)} (${StoreInfo.RATING_COUNT} ulasan)",
                color = fg,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Pill(bg) {
            Text("🕗 ${StoreInfo.HOURS_LABEL}", color = fg, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(8.dp).background(if (isOpen) Basil.copy(alpha = 1f) else Chili, CircleShape))
            Spacer(Modifier.width(4.dp))
            // Box dengan teks "Tutup" transparan sebagai penentu lebar tetap,
            // supaya pergantian status Buka/Tutup tidak mengubah lebar pill (anti-geser).
            Box(contentAlignment = Alignment.CenterStart) {
                Text(
                    "Tutup",
                    color = Color.Transparent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (isOpen) "Buka" else "Tutup",
                    color = fg,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun Pill(background: Color, content: @Composable () -> Unit) {
    Surface(color = background, shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { content() }
    }
}
