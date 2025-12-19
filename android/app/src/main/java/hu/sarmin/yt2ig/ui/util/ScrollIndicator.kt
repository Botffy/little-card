package hu.sarmin.yt2ig.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class ScrollIndicatorColors(
    val trackColor: Color,
    val indicatorColor: Color
)

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
fun ScrollIndicator(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    colors: ScrollIndicatorColors = ScrollIndicatorColors(
        trackColor = MaterialTheme.colorScheme.onSurface,
        indicatorColor = MaterialTheme.colorScheme.primary
    )
) {
    var isScrolling by remember { mutableStateOf(false) }

    // Detect scroll activity
    LaunchedEffect(scrollState.value) {
        isScrolling = true
        delay(700)
        isScrolling = false
    }

    val alpha by animateFloatAsState(
        targetValue = if (isScrolling) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrollIndicatorAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxHeight()
            .alpha(alpha)
    ) {
        val trackHeight = maxHeight

        // Calculate the proportion of visible content to total content
        // viewportSize / (viewportSize + maxScrollValue) = visible ratio
        val viewportRatio = if (scrollState.maxValue > 0) {
            val totalContentSize = scrollState.maxValue + trackHeight.value
            (trackHeight.value / totalContentSize).coerceIn(0.1f, 1f)
        } else {
            1f // If no scrolling is needed, indicator fills the track
        }

        val indicatorHeight = trackHeight * viewportRatio

        val scrollProgress = if (scrollState.maxValue > 0) {
            scrollState.value.toFloat() / scrollState.maxValue.toFloat()
        } else {
            0f
        }

        val indicatorTopOffset = (trackHeight - indicatorHeight) * scrollProgress

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 2.dp)
                .width(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.trackColor.copy(alpha = 0.1f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = indicatorTopOffset)
                .size(width = 4.dp, height = indicatorHeight)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.indicatorColor.copy(alpha = 0.6f))
        )
    }
}
