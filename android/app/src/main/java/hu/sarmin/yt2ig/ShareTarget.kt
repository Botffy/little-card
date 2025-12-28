package hu.sarmin.yt2ig

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

data class ParsedText(val text: String, val parsing: Parsing) {
    fun isValid() = parsing is Parsing.Result
}

sealed interface Parsing {
    interface Error : Parsing {
        object InvalidUrl : Error
        object UnknownShareTarget : Error
        object MultipleUrls : Error

        val errorMessage: ErrorMessage
            get() = ErrorMessage("error_parsing_${this::class.simpleName!!.lowercase()}")
    }
    data class Result(val target: ShareTarget.Valid) : Parsing
}

private const val TAG = "ShareTarget"

/**
 * An intentionally liberal URL regex to extract url candidates which are then validated properly.
 */
private val URL_REGEX = Regex("""
(?xi)
\b
(
    (?:https?://)?                     # Optional protocol
    (?:
        [1-9]\d{0,2}(?:\.\d{1,3}){3}       # IP address
        |
        (?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}   # Domain name
    )
    (?:
        [^\s()<>{}\[\]]*                   # path and query string
        [^\s`!()\[\]{};:'".,<>?«»“”‘’]     # ending is more constrained
    )?
)
""".trimIndent())

fun parse(input: String): Parsing {
    val trimmedInput = input.trim()
    val hasWhitespace = trimmedInput.any { it.isWhitespace() }

    // Try to parse as a direct URL first
    val directHttpUrl = if (!hasWhitespace) {
        try {
            trimmedInput.toHttpUrl()
        } catch (_: IllegalArgumentException) {
            // Maybe missing protocol
            if (!trimmedInput.startsWith("http://") && !trimmedInput.startsWith("https://")) {
                try {
                    "https://$trimmedInput".toHttpUrl()
                } catch (_: IllegalArgumentException) {
                    null // Still no luck
                }
            } else {
                return Parsing.Error.InvalidUrl
            }
        }
    } else {
        null // Has whitespace, treat as text
    }

    if (directHttpUrl != null) {
        Log.d(TAG, "parse: input is a direct URL")
        return getTargetFor(directHttpUrl)
    }

    val urls = URL_REGEX.findAll(trimmedInput)
        .map { match -> toUrlIfPossible(match.value) }
        .filterNotNull()
        .toList()

    return when (urls.size) {
        0 -> {
            Log.i(TAG, "parse: no valid URLs found in input")
            Parsing.Error.InvalidUrl
        }
        1 -> {
            getTargetFor(urls.first())
        }
        else -> {
            Log.i(TAG, "parse: multiple URLs found in input")
            Parsing.Error.MultipleUrls
        }
    }
}

private fun toUrlIfPossible(input: String): HttpUrl? {
    try {
        if (input.startsWith("http://") || input.startsWith("https://")) {
            return input.toHttpUrl()
        }

        return "https://$input".toHttpUrl()
    } catch (e: IllegalArgumentException) {
        return null
    }
}

sealed interface ShareTarget {
    sealed interface Valid : ShareTarget {
        /**
         * Get the canonical url for this target.
         */
        val url: HttpUrl
        val displayUrl: String
            get() = url.toString()

        fun asResult() = Parsing.Result(this)
    }
}

sealed interface YouTubeParsingError : Parsing.Error {
    object NoVideoId : YouTubeParsingError
    object IsChannel : YouTubeParsingError
    object IsPlaylist : YouTubeParsingError
    object NoPath : YouTubeParsingError
    object UnknownPath : YouTubeParsingError
}


enum class YouTubeApp {
    YOUTUBE, YOUTUBE_MUSIC
}

enum class YouTubeVideoType {
    NORMAL, SHORTS, LIVE;

    companion object {
        fun fromPathSegment(segment: String?): YouTubeVideoType =
            when (segment) {
                "shorts" -> SHORTS
                "live" -> LIVE
                else -> throw IllegalStateException("Unexpected path segment for YouTube video type: $segment")
            }
    }
}

private fun shortenedYouTubeUrl(videoId: String): HttpUrl = HttpUrl.Builder()
    .scheme("https")
    .host("youtu.be")
    .addPathSegment(videoId)
    .build()

private fun youTubeMusicUrl(videoId: String): HttpUrl = HttpUrl.Builder()
    .scheme("https")
    .host("music.youtube.com")
    .addPathSegment("watch")
    .addQueryParameter("v", videoId)
    .build()

data class YouTubeVideo(val videoId: String, val type: YouTubeVideoType = YouTubeVideoType.NORMAL, val app: YouTubeApp = YouTubeApp.YOUTUBE) : ShareTarget.Valid {
    init {
        require(videoId.isNotBlank()) { "Invalid video ID" }
    }

    override val url: HttpUrl = when (app) {
        YouTubeApp.YOUTUBE -> shortenedYouTubeUrl(videoId)
        YouTubeApp.YOUTUBE_MUSIC -> youTubeMusicUrl(videoId)
    }

    override val displayUrl: String
        get() = url.toString().substringAfter("https://")
}

fun getTargetFor(uri: HttpUrl): Parsing {
    val host = uri.host.lowercase()

    return when (host) {
        "www.youtube.com", "youtube.com" -> parseYouTubeLongLink(uri)
        "youtu.be" -> parseYouTubeShortLink(uri)
        "music.youtube.com" -> parseYouTubeMusicLink(uri)
        else -> Parsing.Error.UnknownShareTarget
    }
}

private fun parseYouTubeShortLink(uri: HttpUrl): Parsing {
    return uri.pathSegments.firstOrNull()
        ?.takeIf { it.isNotBlank() }
        ?.let { videoId -> YouTubeVideo(videoId).asResult() }
        ?: return YouTubeParsingError.NoVideoId
}

private fun parseYouTubeLongLink(uri: HttpUrl): Parsing {
    val firstSegment = uri.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() } ?: return YouTubeParsingError.NoPath

    return when (firstSegment) {
        "watch" -> parseWatchLink(uri, YouTubeApp.YOUTUBE)
        "shorts", "live" -> {
            uri.pathSegments.getOrNull(1)
                ?.takeIf { it.isNotBlank() }
                ?.let { videoId ->
                    val type = YouTubeVideoType.fromPathSegment(firstSegment)
                    YouTubeVideo(videoId, type, YouTubeApp.YOUTUBE).asResult()
                }
                ?: YouTubeParsingError.NoVideoId
        }
        "channel", "c", "user" -> YouTubeParsingError.IsChannel
        "playlist" -> YouTubeParsingError.IsPlaylist
        else -> YouTubeParsingError.UnknownPath
    }
}

private fun parseYouTubeMusicLink(uri: HttpUrl): Parsing {
    val firstSegment = uri.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() } ?: return YouTubeParsingError.NoPath

    return when (firstSegment) {
        "watch" -> parseWatchLink(uri, YouTubeApp.YOUTUBE_MUSIC)
        "channel" -> YouTubeParsingError.IsChannel
        "playlist" -> YouTubeParsingError.IsPlaylist
        else -> YouTubeParsingError.UnknownPath
    }
}

private fun parseWatchLink(uri: HttpUrl, app: YouTubeApp): Parsing {
    return uri.queryParameter("v")
        ?.takeIf { it.isNotBlank() }
        ?.let { videoId -> YouTubeVideo(
            videoId = videoId,
            app = app
        ).asResult() }
        ?: YouTubeParsingError.NoVideoId
}
