package hu.sarmin.yt2ig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFrame(isHome: Boolean, title: String, goHome: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (!isHome) {
                        IconButton(onClick = goHome) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        }
                    } else {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
fun App(value: State, goHome: () -> Unit) {
    Yt2igTheme {
        Crossfade(targetState = value, label = "state") { current ->
            when (current) {
                is State.Home -> HomeScreen()
                is State.Preview -> PreviewScreen(current.shareTarget, current.loading, goHome)
                is State.Error -> ErrorScreen(current.message, goHome)
            }
        }
    }
}

@Composable
fun HomeScreen() {
    AppFrame(true, "yt2ig", {  }) {
        Text(
            text = "Home Screen",
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        )
    }
}

@Composable
fun PreviewScreen(target: ValidShareTarget, loading: PreviewLoadingState, goHome: () -> Unit) {
    AppFrame(false, "Preview", goHome) {
        when (loading) {
            is PreviewLoadingState.Loading -> Text(
                text = "Loading info for: ${target.url}",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
            is PreviewLoadingState.LoadedInfo -> Text(
                text = "Video Title: ${loading.data.title}\nChannel: ${loading.data.channel}\nURL: ${target.url}",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
            is PreviewLoadingState.LoadedImage -> Text(
                text = "Downloaded thumbnail for video: ${loading.data.title}",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
            is PreviewLoadingState.CreatedPreview -> Image(
                bitmap = loading.previewImage.asImageBitmap(),
                contentDescription = "preview for video: ${loading.data.title}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String, goHome: () -> Unit) {
    AppFrame(false, "Error", goHome) {
        Text(
            text = "Error: $message",
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        )
    }
}
