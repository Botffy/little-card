package hu.sarmin.yt2ig

import android.util.Log
import hu.sarmin.yt2ig.util.HttpClientProvider
import hu.sarmin.yt2ig.util.await
import hu.sarmin.yt2ig.util.getStringOrNull
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONObject

private const val YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/"

data class YouTubeVideoInfo(
    val app: YouTubeApp,
    val title: String,
    val channel: String,
    val thumbnailUrl: HttpUrl
)

sealed interface YouTubeCardCreationError : CardCreationError {
    data object VideoInfoNotFound : YouTubeCardCreationError
    data object YouTubeError : YouTubeCardCreationError
    data object NoThumbnailAvailable : YouTubeCardCreationError

    override fun code() = "error_youtube_${this::class.simpleName!!.lowercase()}"
}

interface YouTubeService {
    suspend fun getVideoInfo(video: YouTubeVideo): YouTubeVideoInfo
}

class RealYouTubeService(private val httpClientProvider: HttpClientProvider, private val apiKey: String) : YouTubeService {
    override suspend fun getVideoInfo(video: YouTubeVideo): YouTubeVideoInfo {
        val url = YOUTUBE_API_URL.toHttpUrl()
            .newBuilder()
            .addPathSegment("videos")
            .addQueryParameter("part", "snippet,contentDetails")
            .addQueryParameter("id", video.videoId)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Goog-Api-Key", apiKey)
            .build()

        httpClientProvider.getClient().await(request).use { response ->
            if (!response.isSuccessful) {
                Log.e("YtApi", "getVideoInfo failed: ${response.code} ${response.message}")
                throw CardCreationException(YouTubeCardCreationError.YouTubeError)
            }

            val jsonResponse = JSONObject(response.body.string())
            val items = jsonResponse.getJSONArray("items")
            if (items.length() == 0) {
                Log.w("YtApi", "No video info found for video ID: ${video.videoId}")
                throw CardCreationException(YouTubeCardCreationError.VideoInfoNotFound)
            }
            val item = items.getJSONObject(0)

            val thumbnailUrl = item.getStringOrNull("snippet.thumbnails.maxres.url") ?:
                item.getStringOrNull("snippet.thumbnails.high.url") ?:
                item.getStringOrNull("snippet.thumbnails.medium.url") ?:
                item.getStringOrNull("snippet.thumbnails.default.url") ?:
                throw CardCreationException(YouTubeCardCreationError.NoThumbnailAvailable)

            val title = item.getJSONObject("snippet").getString("title")
            val uploader = item.getJSONObject("snippet").getString("channelTitle")

            return YouTubeVideoInfo(
                title = title,
                channel = uploader,
                thumbnailUrl = thumbnailUrl.toHttpUrl(),
                app = video.app
            )
        }
    }
}
