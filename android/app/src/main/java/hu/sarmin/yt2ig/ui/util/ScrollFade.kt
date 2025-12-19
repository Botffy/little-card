package hu.sarmin.yt2ig.ui.util

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds a fade-out effect at the top and bottom edges of scrollable content.
 *
 * @param scrollState The scroll state to monitor for scroll position
 * @param backgroundColor The background color to fade to/from
 * @param fadeHeight The height of the fade effect (default: 48.dp)
 */
fun Modifier.scrollFade(
    scrollState: ScrollState,
    backgroundColor: Color,
    fadeHeight: Dp = 48.dp,
): Modifier = this.drawWithContent {
    drawContent()

    val fadeHeightPx = fadeHeight.toPx()

    // Top fade - only show if not at the top
    if (scrollState.value > 0) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundColor,
                    Color.Transparent
                ),
                startY = 0f,
                endY = fadeHeightPx
            )
        )
    }

    // Bottom fade - only show if not at the bottom
    if (scrollState.value < scrollState.maxValue) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    backgroundColor
                ),
                startY = size.height - fadeHeightPx,
                endY = size.height
            )
        )
    }
}
