package hu.sarmin.yt2ig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import hu.sarmin.yt2ig.ui.AppFrame
import hu.sarmin.yt2ig.ui.SharingScreen
import hu.sarmin.yt2ig.ui.StandardScreen
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme

data class AppActions(
    val goHome: () -> Unit,
    val shareToInstaStory: (AppState.Share.LoadingState.Created) -> Unit,
    val shareToOther: (AppState.Share.LoadingState.Created) -> Unit,
    val copyUrl: (ValidShareTarget) -> Unit,
)

val LocalAppActions = staticCompositionLocalOf<AppActions> {
    error("No AppActions provided")
}

@Composable
fun App(value: AppState,
        goHome: () -> Unit,
        shareToInstaStory: (AppState.Share.LoadingState.Created) -> Unit,
        shareToOther: (AppState.Share.LoadingState.Created) -> Unit,
        copyUrl: (ValidShareTarget) -> Unit
) {
    CompositionLocalProvider(LocalAppActions provides AppActions(
        goHome,
        shareToInstaStory,
        shareToOther,
        copyUrl
    )) {
        val stateName = when (value) {
            is AppState.Home -> "Home"
            is AppState.Share -> "Share"
            is AppState.Error -> "Error"
        }

        Yt2igTheme {
            Crossfade(targetState = stateName, label = "state") { _ ->
                when (value) {
                    is AppState.Home -> HomeScreen()
                    is AppState.Share -> SharingScreen(value.shareTarget, value.loading)
                    is AppState.Error -> ErrorScreen(value.message, goHome)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    AppFrame(true, "yt2ig", { }) { padding ->
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
