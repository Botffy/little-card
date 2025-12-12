package hu.sarmin.yt2ig

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ParseYouTubeTest {

    @Test
    fun `parses youtu-be urls`() {
        val videoId = "I_aBmrYChfQ"
        val target = parse("https://youtu.be/${videoId}?si=zPPUyGRKIHvqMvEX")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.NORMAL)
        }
    }

    @Test
    fun `throws for plain youtu-be url without video id`() {
        assertError<YouTubeParsingError.NoVideoId>(parse("https://youtu.be/"))
        assertError<YouTubeParsingError.NoVideoId>(parse("https://youtu.be"))
    }

    @Test
    fun `parses youtube shorts url`() {
        val videoId = "I_aBmrYChfQ"
        val target = parse("https://www.youtube.com/shorts/${videoId}")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.SHORTS)
        }
    }

    @Test
    fun `youtube shorts url without an id is an error`() {
        assertError<YouTubeParsingError.NoVideoId>(parse("https://www.youtube.com/shorts/"))
        assertError<YouTubeParsingError.NoVideoId>(parse("https://www.youtube.com/shorts"))
    }

    @Test
    fun `parses youtube live url`() {
        val videoId = "I_aBmrYChfQ"
        val target = parse("https://www.youtube.com/live/${videoId}")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.LIVE)
        }
    }

    @Test
    fun `youtube link with unknown subpath is an unknown target`() {
        val target = parse("https://www.youtube.com/something-new/video-id")
        assertError<YouTubeParsingError.UnknownPath>(target)
    }

    inline fun <reified T : ShareTarget.Valid> assertResult(actual: Parsing, noinline block: (T) -> Unit) {
        assertThat(actual).isInstanceOf(Parsing.Result::class.java)
        val target = (actual as Parsing.Result).target
        assertThat(target).isInstanceOf(T::class.java)
        block(target as T)
    }

    inline fun <reified T : Parsing.Error> assertError(actual: Parsing, noinline block: (T) -> Unit = {}) {
        assertThat(actual).isInstanceOf(Parsing.Error::class.java)
        block(actual as T)
    }
}

