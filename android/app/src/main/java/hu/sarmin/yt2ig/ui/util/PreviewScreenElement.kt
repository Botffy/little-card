package hu.sarmin.yt2ig.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme

@Composable
fun PreviewScreenElement(content: @Composable () -> Unit) {
    Yt2igTheme {
        Box(
            modifier = Modifier
                .size(480.dp)
        ) {
            content()
        }
    }
}
