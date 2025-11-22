package hu.sarmin.yt2ig

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import hu.sarmin.yt2ig.ui.AppFrame
import hu.sarmin.yt2ig.ui.HomeScreen
import hu.sarmin.yt2ig.ui.SharingScreen
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme

data class AppActions(
    val goHome: () -> Unit,
    val onUrlEntered: (maybeUrl: String) -> Unit,
    val shareToInstaStory: (AppState.Share.LoadingState.Created) -> Unit,
    val shareToOther: (AppState.Share.LoadingState.Created) -> Unit,
    val copyUrl: (ValidShareTarget) -> Unit
)

val LocalAppActions = staticCompositionLocalOf<AppActions> {
    error("No AppActions provided")
}

@Composable
fun App(value: AppState, functions: AppActions) {
    CompositionLocalProvider(LocalAppActions provides functions) {
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
                    is AppState.Error -> ErrorScreen(value.message, functions.goHome)
                }
            }
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
