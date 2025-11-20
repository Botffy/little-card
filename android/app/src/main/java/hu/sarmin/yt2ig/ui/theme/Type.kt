package hu.sarmin.yt2ig.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import hu.sarmin.yt2ig.R

private val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

val Typography = Typography(
    bodyLarge = TextStyle(fontFamily = Inter),
    bodyMedium = TextStyle(fontFamily = Inter),
    bodySmall = TextStyle(fontFamily = Inter),
    titleMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium)
)
