package hu.sarmin.yt2ig

sealed interface AppState {
    data object Home : AppState
    data class Share(val shareTarget: ValidShareTarget, val loading: LoadingState) : AppState {
        sealed interface LoadingState {
            data object Starting : LoadingState
            data class LoadedInfo(val data: YouTubeVideoInfo) : LoadingState

            data class LoadedThumbnail(val data: YouTubeVideoInfo) : LoadingState

            data class Created(val data: YouTubeVideoInfo, val shareCard: ShareCard) : LoadingState
        }
    }
    data class Error(val message: String) : AppState
}

