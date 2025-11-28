package hu.sarmin.yt2ig

sealed interface AppState {
    data object Home : AppState
    data class Share(val shareTarget: ShareTarget.Valid, val loading: LoadingState) : AppState {
        sealed interface LoadingState {
            data class Starting(val target: YouTubeVideo) : LoadingState
            data class LoadedInfo(val target: YouTubeVideo, val data: YouTubeVideoInfo) : LoadingState

            data class LoadedThumbnail(val target: YouTubeVideo, val data: YouTubeVideoInfo) : LoadingState

            data class Created(val target: YouTubeVideo, val data: YouTubeVideoInfo, val shareCard: ShareCard) : LoadingState
        }
    }
    data class Error(val error: ErrorMessage, val params: List<String> = emptyList()) : AppState
}
