package hu.sarmin.yt2ig

import hu.sarmin.yt2ig.util.await
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

private const val YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3/"

data class YouTubeVideoInfo(
    val title: String,
    val channel: String,
    val thumbnailUrl: HttpUrl
)

interface YouTubeService {
    suspend fun getVideoInfo(videoId: String): YouTubeVideoInfo
}

private val httpClient = OkHttpClient()

class RealYouTubeService(private val apiKey: String, private val apiEndpoint: String = YOUTUBE_API_URL) : YouTubeService {
    override suspend fun getVideoInfo(videoId: String): YouTubeVideoInfo {
        val url = apiEndpoint.toHttpUrl()
            .newBuilder()
            .addPathSegment("videos")
            .addQueryParameter("part", "snippet,contentDetails")
            .addQueryParameter("id", videoId)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Goog-Api-Key", apiKey)
            .build()

        httpClient.await(request).use { response ->
            if (!response.isSuccessful) {
                throw IOException("YT API error ${response.code}, ${response.message}")
            }

            val jsonResponse = JSONObject(response.body.string())
            val items = jsonResponse.getJSONArray("items")
            if (items.length() == 0) {
                throw IllegalArgumentException("No video found with ID: $videoId")
            }
            val item = items.getJSONObject(0)

            val thumbnailUrl = item.getJSONObject("snippet")
                .getJSONObject("thumbnails")
                .getJSONObject("default")
            val title = item.getJSONObject("snippet").getString("title")
            val uploader = item.getJSONObject("snippet").getString("channelTitle")

            return YouTubeVideoInfo(
                title = title,
                channel = uploader,
                thumbnailUrl = thumbnailUrl.getString("url").toHttpUrl()
            )
        }
    }
}
