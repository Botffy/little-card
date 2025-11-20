package hu.sarmin.yt2ig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
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
    val shareToInstaStory: (AppState.Share.LoadingState.Created) -> Unit
)

val LocalAppActions = staticCompositionLocalOf<AppActions> {
    error("No AppActions provided")
}

@Composable
fun App(value: AppState, goHome: () -> Unit, shareToInstaStory: (AppState.Share.LoadingState.Created) -> Unit) {
    CompositionLocalProvider(LocalAppActions provides AppActions(goHome, shareToInstaStory)) {
        Yt2igTheme {
            Crossfade(targetState = value, label = "state") { current ->
                when (current) {
                    is AppState.Home -> HomeScreen()
                    is AppState.Share -> SharingScreen(current.shareTarget, current.loading)
                    is AppState.Error -> ErrorScreen(current.message, goHome)
                }
            }
        }
    }
}

@Composable
fun StandardScreen(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        content()
    }
}

@Composable
fun HomeScreen() {
    AppFrame(true, "yt2ig", {  }) { padding ->
        StandardScreen(
            Modifier.padding(padding)
        ) {
            Text(
                text = "Home Screen",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun SharingScreen(target: ValidShareTarget, loading: AppState.Share.LoadingState) {
    val actions = LocalAppActions.current
    AppFrame(false, "Share", actions.goHome) { padding ->
        StandardScreen(
            Modifier.padding(padding)
        ) {
            when (loading) {
                is AppState.Share.LoadingState.Starting -> Text(
                    text = "Loading info for: ${target.url}",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                is AppState.Share.LoadingState.LoadedInfo -> Text(
                    text = "Video Title: ${loading.data.title}\nChannel: ${loading.data.channel}\nURL: ${target.url}",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                is AppState.Share.LoadingState.LoadedThumbnail -> Text(
                    text = "Downloaded thumbnail for video: ${loading.data.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                is AppState.Share.LoadingState.Created -> CreatedShareScreen(loading)
            }
        }
    }
}

@Composable
fun CreatedShareScreen(state: AppState.Share.LoadingState.Created) {
    val actions = LocalAppActions.current
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
