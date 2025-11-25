package hu.sarmin.yt2ig

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

sealed interface Parsing {
    interface Error : Parsing {
        object InvalidUrl : Error
        object UnknownShareTarget : Error

        fun code() = "error_parsing_${this::class.simpleName!!.lowercase()}"
    }
    data class Result(val target: ShareTarget.Valid) : Parsing
}

fun parse(url: String): Parsing {
    val httpUrl = try {
        url.toHttpUrl()
    } catch (e: IllegalArgumentException) {
        return Parsing.Error.InvalidUrl
    }

    return getTargetFor(httpUrl)
}

sealed interface ShareTarget {
    sealed interface Valid : ShareTarget {
        /**
         * Get the canonical url for this target.
         */
        val url: HttpUrl

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

data class YouTubeVideo(val videoId: String, val type: YouTubeVideoType = YouTubeVideoType.NORMAL) : ShareTarget.Valid {
    init {
        require(videoId.isNotBlank()) { "Invalid video ID" }
    }

    // https://youtu.be/O6yP5jgiq34
    override val url: HttpUrl = HttpUrl.Builder()
            .scheme("https")
            .host("youtu.be")
            .addPathSegment(this.videoId)
            .build()
}

fun getTargetFor(uri: HttpUrl): Parsing {
    val host = uri.host.lowercase()

    return when (host) {
        "www.youtube.com", "youtube.com" -> parseYouTubeLongLink(uri)
        "youtu.be" -> parseYouTubeShortLink(uri)
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
        "watch" -> {
            uri.queryParameter("v")
                ?.takeIf { it.isNotBlank() }
                ?.let { videoId -> YouTubeVideo(videoId).asResult() }
                ?: YouTubeParsingError.NoVideoId
        }
        "shorts", "live" -> {
            uri.pathSegments.getOrNull(1)
                ?.takeIf { it.isNotBlank() }
                ?.let { videoId ->
                    val type = YouTubeVideoType.fromPathSegment(firstSegment)
                    YouTubeVideo(videoId, type).asResult()
                }
                ?: YouTubeParsingError.NoVideoId
        }
        "channel", "c", "user" -> YouTubeParsingError.IsChannel
        "playlist" -> YouTubeParsingError.IsPlaylist
        else -> YouTubeParsingError.UnknownPath
    }
}
