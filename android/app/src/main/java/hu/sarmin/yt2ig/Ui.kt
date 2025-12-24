package hu.sarmin.yt2ig

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import hu.sarmin.yt2ig.ui.ErrorScreen
import hu.sarmin.yt2ig.ui.HelpPage
import hu.sarmin.yt2ig.ui.HelpScreen
import hu.sarmin.yt2ig.ui.HomeScreen
import hu.sarmin.yt2ig.ui.SharingScreen
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme

data class AppActions(
    val goHome: () -> Unit,
    val showHelp: (page: HelpPage) -> Unit,
    val back: () -> Unit,
    val parse: (String) -> Parsing,
    val share: (ShareTarget.Valid) -> Unit,
    val shareToInstaStory: (AppState.Share.LoadingState.Created) -> Unit,
    val shareToOther: (AppState.Share.LoadingState.Created) -> Unit,
    val copyUrl: (ShareTarget.Valid) -> Unit,
    val toMessage: (ErrorMessage) -> String
)

val LocalAppActions = staticCompositionLocalOf<AppActions> {
    error("No AppActions provided")
}

@Composable
fun App(value: AppState, functions: AppActions, getContext: () -> Context) {
    CompositionLocalProvider(LocalAppActions provides functions) {
        Yt2igTheme {
            AnimatedContent(
                targetState = value,
                label = "state",
                contentKey = { state ->
                    when (state) {
                        is AppState.Home -> "home"
                        is AppState.Help -> "help"
                        is AppState.Share -> "share"
                        is AppState.Error -> "error"
                    }
                },
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 500,
                            delayMillis = 90
                        )
                    ) togetherWith fadeOut(
                        animationSpec = tween(
                            durationMillis = 200
                        )
                    )
                }
            ) { state ->
                when (state) {
                    is AppState.Home -> HomeScreen()
                    is AppState.Help -> HelpScreen(state.page)
                    is AppState.Share -> SharingScreen(state.shareTarget, state.loading)
                    is AppState.Error -> ErrorScreen(state.error.toMessage(getContext()), state.rawInput, functions.goHome)
                }
            }
        }
    }
}
