package id.biz.majulancar.kasir.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.floor

private const val QUIET_ZONE_MODULES = 2

private fun buildMatrix(content: String): BitMatrix? = try {
    val hints = mapOf<EncodeHintType, Any>(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 0,
        EncodeHintType.CHARACTER_SET to "UTF-8",
    )
    QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
} catch (e: WriterException) {
    null
} catch (e: IllegalArgumentException) {
    null
}

/** QR hitam di atas putih (selalu, termasuk mode gelap) dengan quiet zone, digambar lokal tanpa internet. */
@Composable
fun QrCodeImage(content: String, modifier: Modifier = Modifier) {
    val matrix = remember(content) { buildMatrix(content) }
    Canvas(
        modifier = modifier
            .background(Color.White)
            .semantics { contentDescription = "Kode QR pembayaran QRIS" },
    ) {
        val m = matrix ?: return@Canvas
        val cells = m.width + QUIET_ZONE_MODULES * 2
        val cell = floor(size.minDimension / cells).coerceAtLeast(1f)
        val used = cell * cells
        val originX = (size.width - used) / 2f
        val originY = (size.height - used) / 2f
        for (y in 0 until m.height) {
            for (x in 0 until m.width) {
                if (m.get(x, y)) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(originX + (x + QUIET_ZONE_MODULES) * cell, originY + (y + QUIET_ZONE_MODULES) * cell),
                        size = Size(cell, cell),
                    )
                }
            }
        }
    }
}
