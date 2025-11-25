package hu.sarmin.yt2ig

class FakeYouTubeService : YouTubeService {
    private val videoInfos = mutableMapOf<String, YouTubeVideoInfo>()

    fun reset() = videoInfos.clear()
    fun addVideoInfo(videoId: String, info: YouTubeVideoInfo) {
        videoInfos[videoId] = info
    }

    override suspend fun getVideoInfo(videoId: String): YouTubeVideoInfo {
        return videoInfos[videoId] ?: throw IllegalArgumentException("No video info for videoId: $videoId")
    }
}
