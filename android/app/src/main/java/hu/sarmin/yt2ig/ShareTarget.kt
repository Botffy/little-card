package hu.sarmin.yt2ig

import okhttp3.HttpUrl

sealed interface ShareTarget

object UnknownShareTarget : ShareTarget

object NoShareTarget : ShareTarget

sealed interface ValidShareTarget : ShareTarget {
    /**
     * Get the canonical url for this target.
     */
    val url: HttpUrl
}

enum class YouTubeVideoType {
    NORMAL, SHORTS, LIVE;

    companion object {
        fun fromPathSegment(segment: String?): YouTubeVideoType =
            when (segment) {
                "shorts" -> YouTubeVideoType.SHORTS
                "live" -> YouTubeVideoType.LIVE
                else -> throw IllegalStateException("Unexpected path segment for YouTube video type: $segment")
            }
    }
}

data class YouTubeVideo(val videoId: String, val type: YouTubeVideoType = YouTubeVideoType.NORMAL) : ValidShareTarget {
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

fun getTargetFor(uri: HttpUrl): ShareTarget {
    val host = uri.host.lowercase()

    return when (host) {
        "www.youtube.com", "youtube.com" -> parseYouTubeLongLink(uri)
        "youtu.be" -> parseYouTubeShortLink(uri)
        else -> UnknownShareTarget
    }
}

private fun parseYouTubeShortLink(uri: HttpUrl): ShareTarget {
    val videoId = uri.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException()
    return YouTubeVideo(videoId)
}

private fun parseYouTubeLongLink(uri: HttpUrl): ShareTarget {
    val firstSegment = uri.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException()

    return when (firstSegment) {
        "watch" -> {
            val id = uri.queryParameter("v") ?: return UnknownShareTarget
            YouTubeVideo(id, YouTubeVideoType.NORMAL)
        }
        "live", "shorts" -> {
            val id = uri.pathSegments.getOrNull(1)?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException()
            YouTubeVideo(id, YouTubeVideoType.fromPathSegment(firstSegment))
        }
        else -> UnknownShareTarget
    }
}
