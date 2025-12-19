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
    fadeHeight: Dp = 32.dp,
): Modifier = this.drawWithContent {
    drawContent()

    val fadeHeightPx = fadeHeight.toPx()

    // Top fade - grows gradually as you scroll down
    if (scrollState.value > 0) {
        val actualTopFadeHeight = minOf(fadeHeightPx, scrollState.value.toFloat())
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundColor,
                    Color.Transparent
                ),
                startY = 0f,
                endY = actualTopFadeHeight
            )
        )
    }

    // Bottom fade - grows gradually as you approach the bottom
    val remainingScroll = scrollState.maxValue - scrollState.value
    if (remainingScroll > 0) {
        val actualBottomFadeHeight = minOf(fadeHeightPx, remainingScroll.toFloat())
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    backgroundColor
                ),
                startY = size.height - actualBottomFadeHeight,
                endY = size.height
            )
        )
    }
}
