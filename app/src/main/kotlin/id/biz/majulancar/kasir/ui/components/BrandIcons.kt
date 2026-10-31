package id.biz.majulancar.kasir.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

private val FacebookBlue = Color(0xFF1877F2)

/** Ikon Facebook: kotak biru membulat + huruf "f" putih (digambar dalam kisi 24x24). */
@Composable
fun FacebookIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        val s = size.minDimension
        drawRoundRect(color = FacebookBlue, cornerRadius = CornerRadius(s * 0.22f, s * 0.22f))
        val f = Path().apply {
            moveTo(13.5f, 21f)
            lineTo(13.5f, 13f)
            lineTo(16f, 13f)
            lineTo(16.5f, 10f)
            lineTo(13.5f, 10f)
            lineTo(13.5f, 8.3f)
            cubicTo(13.5f, 7.5f, 13.8f, 7f, 14.9f, 7f)
            lineTo(16.6f, 7f)
            lineTo(16.6f, 4.3f)
            cubicTo(16.3f, 4.25f, 15.4f, 4.2f, 14.4f, 4.2f)
            cubicTo(12.2f, 4.2f, 10.5f, 5.5f, 10.5f, 8f)
            lineTo(10.5f, 10f)
            lineTo(8f, 10f)
            lineTo(8f, 13f)
            lineTo(10.5f, 13f)
            lineTo(10.5f, 21f)
            close()
        }
        scale(scale = s / 24f, pivot = Offset.Zero) {
            drawPath(path = f, color = Color.White)
        }
    }
}

/** Ikon Instagram: kotak gradien membulat + kamera (bingkai, lensa, titik lampu) putih. */
@Composable
fun InstagramIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        val s = size.minDimension
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFEDA77),
                    Color(0xFFF58529),
                    Color(0xFFDD2A7B),
                    Color(0xFF8134AF),
                    Color(0xFF515BD4),
                ),
                start = Offset(0f, s),
                end = Offset(s, 0f),
            ),
            cornerRadius = CornerRadius(s * 0.28f, s * 0.28f),
        )
        val stroke = Stroke(width = s * 0.085f)
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(s * 0.2f, s * 0.2f),
            size = Size(s * 0.6f, s * 0.6f),
            cornerRadius = CornerRadius(s * 0.17f, s * 0.17f),
            style = stroke,
        )
        drawCircle(color = Color.White, radius = s * 0.14f, center = Offset(s * 0.5f, s * 0.5f), style = stroke)
        drawCircle(color = Color.White, radius = s * 0.04f, center = Offset(s * 0.69f, s * 0.31f))
    }
}
