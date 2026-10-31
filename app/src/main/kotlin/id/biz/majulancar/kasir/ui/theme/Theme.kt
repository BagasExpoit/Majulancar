package id.biz.majulancar.kasir.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Palet diambil dari styles/_variables.scss situs asli.
val Chili = Color(0xFFC73029)
val ChiliDeep = Color(0xFF9C231D)
val Turmeric = Color(0xFFE2A53D)
val Basil = Color(0xFF4C7A5E)
private val Charcoal = Color(0xFF1C1712)
private val Cream = Color(0xFFFBF7F0)
private val Cream2 = Color(0xFFF3ECDF)
private val Ink = Color(0xFF362B22)
private val InkSoft = Color(0xFF6B5C4D)

private val LightColors = lightColorScheme(
    primary = Chili,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFBE3E0),
    onPrimaryContainer = ChiliDeep,
    secondary = Basil,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEBE1),
    onSecondaryContainer = Color(0xFF1F3A2A),
    tertiary = Turmeric,
    onTertiary = Charcoal,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Cream2,
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFD9CFC0),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE5645C),
    onPrimary = Charcoal,
    primaryContainer = Color(0xFF5A1D19),
    onPrimaryContainer = Color(0xFFFBE3E0),
    secondary = Color(0xFF86B79A),
    onSecondary = Charcoal,
    secondaryContainer = Color(0xFF2E4A3A),
    onSecondaryContainer = Color(0xFFDCEBE1),
    tertiary = Turmeric,
    onTertiary = Charcoal,
    background = Charcoal,
    onBackground = Color(0xFFF2E9DC),
    surface = Color(0xFF241D16),
    onSurface = Color(0xFFF2E9DC),
    surfaceVariant = Color(0xFF2B231A),
    onSurfaceVariant = Color(0xFFC9BBA8),
    outline = Color(0xFF4A3F33),
    error = Color(0xFFF2B8B5),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
)

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content,
    )
}
