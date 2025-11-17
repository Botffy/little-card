package hu.sarmin.yt2ig

sealed interface State {
    data object Home : State
    data class Preview(val shareTarget: ValidShareTarget) : State
    data class Error(val message: String) : State
}
