package hu.sarmin.yt2ig

import hu.sarmin.yt2ig.ui.HelpPage

sealed interface Input {
    data class Raw(val rawText: String) : Input
    data class Parsed(val parsedText: ParsedText) : Input
}

sealed interface AppState {
    data class Home(val data: Input, val fromClipboard: Boolean = false) : AppState {
        constructor() : this(Input.Raw(""), false)

        fun isEmpty(): Boolean = data is Input.Raw && data.rawText.isBlank()
    }
    data class Help(val page: HelpPage): AppState
    data class Share(val shareTarget: ShareTarget.Valid, val loading: LoadingState) : AppState {
        sealed interface LoadingState {
            data class Starting(val target: YouTubeVideo) : LoadingState
            data class LoadedInfo(val target: YouTubeVideo, val data: YouTubeVideoInfo) : LoadingState

            data class LoadedThumbnail(val target: YouTubeVideo, val data: YouTubeVideoInfo) : LoadingState

            data class Created(val target: YouTubeVideo, val data: YouTubeVideoInfo, val shareCard: ShareCard) : LoadingState
        }
    }
    data class Error(val error: ErrorMessage, val input: ParsedText?) : AppState
}
