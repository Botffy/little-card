package hu.sarmin.yt2ig

import android.content.Context
import hu.sarmin.yt2ig.util.await
import hu.sarmin.yt2ig.util.getHttpClient
import okhttp3.HttpUrl
import java.io.IOException

interface ImageLoader {
    enum class PresetImageId {
        YOUTUBE_LOGO,
    }

    suspend fun fetchImage(url: HttpUrl): ByteArray
    suspend fun ensureLoaded(url: HttpUrl)
    fun fetchPresetImage(id: ImageLoader.PresetImageId): ByteArray
}

class RealImageLoader(context: Context): ImageLoader {
    private val cache = mutableMapOf<String, ByteArray>()

    init {
        cache[ImageLoader.PresetImageId.YOUTUBE_LOGO.name] = context.assets.open("logos/yt_icon_red_digital.png").readBytes()
    }

    override fun fetchPresetImage(id: ImageLoader.PresetImageId): ByteArray {
        return cache[id.name]!!
    }

    override suspend fun fetchImage(url: HttpUrl): ByteArray {
        val id = url.toString()
        if (!cache.containsKey(id)) {
            val bytes = download(url)
            cache[id] = bytes
        }

        return cache[url.toString()]!!
    }

    override suspend fun ensureLoaded(url: HttpUrl) {
        val id = url.toString()
        if (!cache.containsKey(id)) {
            val bytes = download(url)
            cache[id] = bytes
        }
    }

    private suspend fun download(url: HttpUrl): ByteArray {
        val request = okhttp3.Request.Builder()
            .url(url)
            .build()

        getHttpClient().await(request).use { response ->
            if (!response.isSuccessful) {
                throw IOException("Image download error ${response.code}, ${response.message}")
            }

            return response.body.bytes()
        }
    }
}
