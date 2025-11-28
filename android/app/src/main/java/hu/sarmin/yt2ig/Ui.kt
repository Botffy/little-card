package hu.sarmin.yt2ig

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import hu.sarmin.yt2ig.ui.ErrorScreen
import hu.sarmin.yt2ig.ui.HomeScreen
import hu.sarmin.yt2ig.ui.SharingScreen
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme

data class AppActions(
    val goHome: () -> Unit,
    val onUrlEntered: (maybeUrl: String) -> Unit,
    val shareToInstaStory: (AppState.Share.LoadingState.Created) -> Unit,
    val shareToOther: (AppState.Share.LoadingState.Created) -> Unit,
    val copyUrl: (ShareTarget.Valid) -> Unit
)

val LocalAppActions = staticCompositionLocalOf<AppActions> {
    error("No AppActions provided")
}

@Composable
fun App(value: AppState, functions: AppActions, getContext: () -> Context) {
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
                    is AppState.Error -> ErrorScreen(value.error.toMessage(getContext()), functions.goHome)
                }
            }
        }
    }
}
