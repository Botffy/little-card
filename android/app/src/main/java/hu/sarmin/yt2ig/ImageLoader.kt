package hu.sarmin.yt2ig

import hu.sarmin.yt2ig.util.HttpClientProvider
import hu.sarmin.yt2ig.util.await
import okhttp3.HttpUrl
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

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
                throw IOException("Image download error ${response.code}, ${response.message}")
            }

            return response.body.bytes()
        }
    }
}
