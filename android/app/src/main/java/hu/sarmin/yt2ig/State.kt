package hu.sarmin.yt2ig

sealed interface State {
    data object Home : State
    data class Preview(val shareTarget: ValidShareTarget, val loading: PreviewLoadingState) : State
    data class Error(val message: String) : State
}

sealed interface PreviewLoadingState {
    data object Loading : PreviewLoadingState
    data class LoadedInfo(val data: YouTubeVideoInfo) : PreviewLoadingState

    data class LoadedThumbnail(val data: YouTubeVideoInfo) : PreviewLoadingState

    data class CreatedPreview(val data: YouTubeVideoInfo, val shareCard: ShareCard) : PreviewLoadingState
}
