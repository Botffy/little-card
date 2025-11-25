package hu.sarmin.yt2ig.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun ErrorScreen(message: String, goHome: () -> Unit) {
    AppFrame(false, "Error", goHome) { padding ->
        StandardScreen(
            Modifier.padding(padding)
        ) {
            Text(
                text = "Error: $message",
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}
