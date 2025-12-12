package hu.sarmin.yt2ig.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun TextWithEmoji(
    text: String,
    emoji: String = "✨",
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = style.toSpanStyle()
                    .copy(color = color.copy(alpha = if (isSystemInDarkTheme()) 0.6f else 1f))
            ) { append(emoji) }

            withStyle(
                style = style.toSpanStyle()
                    .copy(color = color)
            ) {
                append(" ")
                append(text)
            }
        }
    )
}
