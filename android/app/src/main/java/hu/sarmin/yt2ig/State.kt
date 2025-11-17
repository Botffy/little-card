package hu.sarmin.yt2ig

sealed interface State {
    data object Home : State
    data class Preview(val shareTarget: ValidShareTarget, val loading: LoadingState) : State
    data class Error(val message: String) : State
}

sealed interface LoadingState {
    data object Loading : LoadingState
    data class Loaded(val data: YouTubeVideoInfo) : LoadingState
}
