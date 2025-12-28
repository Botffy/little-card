package hu.sarmin.yt2ig

import android.content.Context

interface ImageStore {
    enum class PresetImageId {
        YOUTUBE_LOGO,
        YOUTUBE_MUSIC_LOGO,
    }

    fun fetchPresetImage(id: PresetImageId): ByteArray
}

class DefaultImageStore(context: Context) : ImageStore {

    private val cache = mutableMapOf<String, ByteArray>()

    init {
        cache[ImageStore.PresetImageId.YOUTUBE_LOGO.name] = context.assets.open("logos/yt_icon_red_digital.png").readBytes()
        cache[ImageStore.PresetImageId.YOUTUBE_MUSIC_LOGO.name] = context.assets.open("logos/youtube_music.png").readBytes()
    }

    override fun fetchPresetImage(id: ImageStore.PresetImageId): ByteArray {
        return cache[id.name]!!
    }
}
