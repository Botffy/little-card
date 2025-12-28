package hu.sarmin.yt2ig

import hu.sarmin.yt2ig.testutil.EXISTING_VIDEO_ID
import hu.sarmin.yt2ig.testutil.TEST_API_KEY
import hu.sarmin.yt2ig.testutil.YouTubeSimulator
import hu.sarmin.yt2ig.testutil.withDispatcher
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class RealYouTubeServiceTest {
    val dispatcher = YouTubeSimulator()

    @Test
    fun `getVideoInfo returns correct info for valid video id`() {
        MockWebServer().withDispatcher(dispatcher) { _, clientProvider ->
            val service = RealYouTubeService(clientProvider, TEST_API_KEY)
            val result = runBlocking { service.getVideoInfo(YouTubeVideo(EXISTING_VIDEO_ID)) }
            assertEquals("https://i.ytimg.com/vi/I_aBmrYChfQ/hqdefault.jpg", result.thumbnailUrl.toString())
            assertEquals("Pixies - Where is my mind", result.title)
            assertEquals("CasaAzul65", result.channel)
        }
    }

    @Test
    fun `getVideoInfo throws exception for non-existing video id`() {
        MockWebServer().withDispatcher(dispatcher) { _, clientProvider  ->
            val service = RealYouTubeService(clientProvider, TEST_API_KEY)
            assertThrows<CardCreationException> { runBlocking { service.getVideoInfo(YouTubeVideo("nonesuch")) } }
        }
    }
}
