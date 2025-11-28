package hu.sarmin.yt2ig

import android.util.Log
import hu.sarmin.yt2ig.util.HttpClientProvider
import hu.sarmin.yt2ig.util.await
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

sealed interface ImageLoaderError : CardCreationError {
    data object ImageDownloadFailed : ImageLoaderError

    override fun code() = "error_image_loader_${this::class.simpleName!!.lowercase()}"
}

interface ImageLoader {
    suspend fun fetchImage(url: HttpUrl): ByteArray
    suspend fun ensureLoaded(url: HttpUrl)
}

class RealImageLoader(private val httpClientProvider: HttpClientProvider): ImageLoader {
    private val cache = ConcurrentHashMap<String, ByteArray>()

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

        httpClientProvider.getClient().await(request).use { response ->
            if (!response.isSuccessful) {
                Log.w("ImageLoader", "downloading $url failed: ${response.code} ${response.message}")
                throw CardCreationException(ImageLoaderError.ImageDownloadFailed)
            }

            return response.body.bytes()
        }
    }
}
