package hu.sarmin.yt2ig.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6F61),
    error = Color(0xFFD04A5C),
    onPrimary = Color(0xFF1D1D1D),
    outline = Color(0xFFE45F55),
    outlineVariant = Color(0xFFB43F51),
    secondary = Color(0xFFFFD168),
    onSecondary = Color(0xFF1A1A1A),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFEDEDED),
    surface = Color(0xFF242424),
    surfaceVariant = Color(0xFF2E2C30),
    onSurface = Color(0xFFDFDFDF),
    onSurfaceVariant = Color(0xFFA8A8A8),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF6F61),
    error = Color(0xFFD04A5C),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFFFC947),
    onSecondary = Color(0xFF1E1E1E),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF2E2E2E),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF6EDEE),
    onSurface = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFF5A5A5A),
)

private val shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun Yt2igTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        shapes = shapes,
        colorScheme = colorScheme,
        typography = interTypography,
        content = content
    )
}
