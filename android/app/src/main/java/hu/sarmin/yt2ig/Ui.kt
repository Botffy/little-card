package hu.sarmin.yt2ig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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

data class AppActions(
    val goHome: () -> Unit,
    val shareToInstaStory: (PreviewLoadingState.CreatedPreview) -> Unit
)

val LocalAppActions = staticCompositionLocalOf<AppActions> {
    error("No AppActions provided")
}

@Composable
fun App(value: State, goHome: () -> Unit, shareToInstaStory: (PreviewLoadingState.CreatedPreview) -> Unit) {
    CompositionLocalProvider(LocalAppActions provides AppActions(goHome, shareToInstaStory)) {
        Yt2igTheme {
            Crossfade(targetState = value, label = "state") { current ->
                when (current) {
                    is State.Home -> HomeScreen()
                    is State.Preview -> PreviewScreen(current.shareTarget, current.loading)
                    is State.Error -> ErrorScreen(current.message, goHome)
                }
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
fun PreviewScreen(target: ValidShareTarget, loading: PreviewLoadingState) {
    val actions = LocalAppActions.current
    AppFrame(false, "Preview", actions.goHome) {
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
            is PreviewLoadingState.LoadedThumbnail -> Text(
                text = "Downloaded thumbnail for video: ${loading.data.title}",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            )
            is PreviewLoadingState.CreatedPreview -> CreatedPreviewScreen(loading, it)
        }
    }
}

@Composable
fun CreatedPreviewScreen(state: PreviewLoadingState.CreatedPreview, padding: PaddingValues) {
    val actions = LocalAppActions.current
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Image(
            bitmap = state.shareCard.image.asImageBitmap(),
            contentDescription = "preview for video: ${state.data.title}",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
        )
        Button(
            onClick = { actions.shareToInstaStory(state) }
        ) { Text("Make it an Insta story") }
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
