package hu.sarmin.yt2ig

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParseTest {

    @Test
    fun `parses youtu-be urls`() {
        val videoId = "I_aBmrYChfQ"
        val target = getTargetFor("https://youtu.be/${videoId}?si=zPPUyGRKIHvqMvEX".toHttpUrl())

        assertTarget<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.NORMAL)
        }
    }

    @Test
    fun `throws for plain youtu-be url without video id`() {
        assertThrows<IllegalArgumentException> { getTargetFor("https://youtu.be/".toHttpUrl()) }
        assertThrows<IllegalArgumentException> { getTargetFor("https://youtu.be".toHttpUrl()) }
    }

    @Test
    fun `parses youtube shorts url`() {
        val videoId = "I_aBmrYChfQ"
        val target = getTargetFor("https://www.youtube.com/shorts/${videoId}".toHttpUrl())

        assertTarget<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.SHORTS)
        }
    }

    @Test
    fun `youtube shorts url without an id is an error`() {
        assertThrows<IllegalArgumentException> { getTargetFor("https://www.youtube.com/shorts/".toHttpUrl()) }
        assertThrows<IllegalArgumentException> { getTargetFor("https://www.youtube.com/shorts".toHttpUrl()) }
    }

    @Test
    fun `parses youtube live url`() {
        val videoId = "I_aBmrYChfQ"
        val target = getTargetFor("https://www.youtube.com/live/${videoId}".toHttpUrl())

        assertTarget<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.LIVE)
        }
    }

    @Test
    fun `youtube link with unknown subpath is an unknown target`() {
        val target = getTargetFor("https://www.youtube.com/something-new/video-id".toHttpUrl())
        assertThat(target).isInstanceOf(UnknownShareTarget::class.java)
    }


    inline fun <reified T : ShareTarget> assertTarget(actual: ShareTarget, noinline block: (T) -> Unit) {
        assertThat(actual).isInstanceOf(T::class.java)
        block(actual as T)
    }
}

