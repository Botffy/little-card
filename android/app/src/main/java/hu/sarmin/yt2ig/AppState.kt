package hu.sarmin.yt2ig

import hu.sarmin.yt2ig.ui.HelpPage

sealed interface AppState {
    data object Home : AppState
    data class Help(val page: HelpPage): AppState
    data class Share(val shareTarget: ShareTarget.Valid, val loading: LoadingState) : AppState {
        sealed interface LoadingState {
            data class Starting(val target: YouTubeVideo) : LoadingState
            data class LoadedInfo(val target: YouTubeVideo, val data: YouTubeVideoInfo) : LoadingState

            data class LoadedThumbnail(val target: YouTubeVideo, val data: YouTubeVideoInfo) : LoadingState

            data class Created(val target: YouTubeVideo, val data: YouTubeVideoInfo, val shareCard: ShareCard) : LoadingState
        }
    }
    data class Error(val error: ErrorMessage, val rawInput: String) : AppState
}
