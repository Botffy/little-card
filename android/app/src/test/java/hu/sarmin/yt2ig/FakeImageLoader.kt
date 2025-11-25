package hu.sarmin.yt2ig

import okhttp3.HttpUrl
import okio.IOException

class FakeImageLoader : ImageLoader {
    val availableUrls = mutableSetOf<HttpUrl>()

    fun addImage(url: HttpUrl) {
        availableUrls.add(url)
    }
    fun reset() {
        availableUrls.clear()
    }

    override suspend fun fetchImage(url: HttpUrl): ByteArray {
        if (availableUrls.contains(url)) {
            return ByteArray(10)
        } else {
            throw IOException("url not available: $url")
        }
    }

    override suspend fun ensureLoaded(url: HttpUrl) {
        if (!availableUrls.contains(url)) {
            throw IOException("url not available: $url")
        }
    }

    override fun fetchPresetImage(id: ImageLoader.PresetImageId): ByteArray {
        return ByteArray(10)
    }
}
