package hu.sarmin.yt2ig

import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test

class ParseYouTubeTest {
    private val videoId = "I_aBmrYChfQ"

    @Test
    fun `parses youtu-be urls`() {
        val target = parse("https://youtu.be/${videoId}?si=zPPUyGRKIHvqMvEX")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.NORMAL)
            assertThat(video.url).isEqualTo("https://youtu.be/$videoId".toHttpUrl())
        }
    }

    @Test
    fun `throws for plain youtu-be url without video id`() {
        assertError<YouTubeParsingError.NoVideoId>(parse("https://youtu.be/"))
        assertError<YouTubeParsingError.NoVideoId>(parse("https://youtu.be"))
    }

    @Test
    fun `parses youtube shorts url`() {
        val target = parse("https://www.youtube.com/shorts/${videoId}")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.SHORTS)
            assertThat(video.app).isEqualTo(YouTubeApp.YOUTUBE)
            assertThat(video.url).isEqualTo("https://www.youtube.com/shorts/$videoId".toHttpUrl())
        }
    }

    @Test
    fun `youtube shorts url without an id is an error`() {
        assertError<YouTubeParsingError.NoVideoId>(parse("https://www.youtube.com/shorts/"))
        assertError<YouTubeParsingError.NoVideoId>(parse("https://www.youtube.com/shorts"))
    }

    @Test
    fun `parses youtube live url`() {
        val target = parse("https://www.youtube.com/live/${videoId}")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.LIVE)
            assertThat(video.app).isEqualTo(YouTubeApp.YOUTUBE)
            assertThat(video.url).isEqualTo("https://www.youtube.com/live/$videoId".toHttpUrl())
        }
    }

    @Test
    fun `youtube link with unknown subpath is an unknown target`() {
        val target = parse("https://www.youtube.com/something-new/video-id")
        assertError<YouTubeParsingError.UnknownPath>(target)
    }

    @Test
    fun `extracts URL from text with prefix content`() {
        val text = "Check this out! https://www.youtube.com/watch?v=$videoId"
        val target = parse(text)

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
        }
    }

    @Test
    fun `extracts URL from text with suffix content`() {
        val text = "https://youtu.be/$videoId - amazing video"
        val target = parse(text)

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
        }
    }

    @Test
    fun `extracts URL from text with both prefix and suffix`() {
        val text = "Hey, watch this: https://www.youtube.com/shorts/$videoId - it's hilarious!"
        val target = parse(text)

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.SHORTS)
        }
    }

    @Test
    fun `extracts URL from multiline text`() {
        val text = """
            Hey there!
            Check out this awesome video:
            https://youtu.be/$videoId
            Let me know what you think!
        """.trimIndent()
        val target = parse(text)

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
        }
    }

    @Test
    fun `returns error when text contains multiple URLs`() {
        val text = "Compare these: https://youtu.be/video1 and https://youtu.be/video2"
        val target = parse(text)
        assertError<Parsing.Error.MultipleUrls>(target)
    }

    @Test
    fun `returns error when text contains three URLs`() {
        val text = "Check https://youtu.be/a or https://youtu.be/b or maybe https://youtu.be/c"
        val target = parse(text)
        assertError<Parsing.Error.MultipleUrls>(target)
    }

    @Test
    fun `returns error when text has no URL at all`() {
        val text = "This is just plain text without any links"
        val target = parse(text)
        assertError<Parsing.Error.InvalidUrl>(target)
    }

    @Test
    fun `returns error when text has malformed URL-like content`() {
        val text = "Check out http:// incomplete URL"
        val target = parse(text)
        assertError<Parsing.Error.InvalidUrl>(target)
    }

    @Test
    fun `extracts youtube watch URL with query parameters from text`() {
        val text = "Never gonna give you up: https://www.youtube.com/watch?v=$videoId&feature=share"
        val target = parse(text)

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
        }
    }

    @Test
    fun `parses youtu-be url without protocol`() {
        val target = parse("youtu.be/$videoId")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.NORMAL)
        }
    }

    @Test
    fun `parses youtube watch url without protocol`() {
        val target = parse("www.youtube.com/watch?v=$videoId")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.NORMAL)
        }
    }

    @Test
    fun `handles text with url and punctuation`() {
        val target = parse("The cat did WHAT? Check out www.youtube.com/watch?v=$videoId!")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.NORMAL)
        }
    }

    @Test
    fun `handles URL with trailing period`() {
        val target = parse("Check this video: https://youtu.be/$videoId.")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
        }
    }

    @Test
    fun `handles URL with trailing comma`() {
        val target = parse("Videos: https://youtu.be/$videoId, and more")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
        }
    }

    @Test
    fun `handles URL with trailing question mark`() {
        val target = parse("Have you seen youtu.be/$videoId?")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
        }
    }

    @Test
    fun `handles URL with trailing semicolon`() {
        val target = parse("Links: www.youtube.com/shorts/$videoId; other stuff")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.SHORTS)
            assertThat(video.app).isEqualTo(YouTubeApp.YOUTUBE)
        }
    }

    @Test
    fun `handles YouTube Music urls`() {
        val target = parse("https://music.youtube.com/watch?v=$videoId")

        assertResult<YouTubeVideo>(target) { video ->
            assertThat(video.videoId).isEqualTo(videoId)
            assertThat(video.type).isEqualTo(YouTubeVideoType.NORMAL)
            assertThat(video.app).isEqualTo(YouTubeApp.YOUTUBE_MUSIC)
            assertThat(video.url.toString()).isEqualTo("https://music.youtube.com/watch?v=$videoId")
        }
    }

    @Test
    fun `YouTube Music doesn't have shorts`() {
        assertError<YouTubeParsingError.UnknownPath>(parse("https://music.youtube.com/shorts/$videoId"))
    }

    @Test
    fun `YouTube Music doesn't have live`() {
        assertError<YouTubeParsingError.UnknownPath>(parse("https://music.youtube.com/live/$videoId"))
    }

    @Test
    fun `YouTube Music channels aren't handled`() {
        assertError<YouTubeParsingError.IsChannel>(parse("https://music.youtube.com/channel/UCp0uxdUViQ2LTAqRePby68g"))
    }

    @Test
    fun `YouTube Music playlists aren't handled`() {
        assertError<YouTubeParsingError.IsPlaylist>(parse("https://music.youtube.com/playlist?list=LRYRch3uwCwj7NwTXoqianhkVWtIL9fcX_GId&si=1PdCeQbYxdvFRVIl"))
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

