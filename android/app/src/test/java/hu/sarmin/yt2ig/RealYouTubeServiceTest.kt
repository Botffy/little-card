package hu.sarmin.yt2ig

import kotlinx.coroutines.runBlocking
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


const val TEST_API_KEY = "test-api-key"
const val EXISTING_VIDEO_ID = "I_aBmrYChfQ"

class RealYouTubeServiceTest {
    val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            when (request.url.encodedPath) {
                "/videos" -> {
                    val videoId = request.url.queryParameter("id")
                    if (videoId == EXISTING_VIDEO_ID) {
                        val responseBody =
                            this.javaClass.getResource("/youtube_json/youtube_video_response.json")!!
                                .readText()

                        return MockResponse.Builder()
                            .code(200)
                            .body(responseBody)
                            .build()
                    } else {
                        return MockResponse.Builder()
                            .code(200)
                            .body(this.javaClass.getResource("/youtube_json/no_result_response.json")!!.readText())
                            .build()
                    }
                }

                else -> {
                    return MockResponse.Builder()
                        .code(404)
                        .body("Not Found")
                        .build()
                }
            }
        }
    }

    @Test
    fun `getVideoInfo returns correct info for valid video id`() {
        MockWebServer().use { mockWebServer ->
            mockWebServer.dispatcher = dispatcher
            mockWebServer.start()

            val service = RealYouTubeService(TEST_API_KEY, mockWebServer.url("/").toString())
            val result = runBlocking { service.getVideoInfo(EXISTING_VIDEO_ID) }
            assertEquals("https://i.ytimg.com/vi/I_aBmrYChfQ/default.jpg", result.thumbnailUrl.toString())
            assertEquals("Pixies - Where is my mind", result.title)
            assertEquals("CasaAzul65", result.channel)
        }
    }

    @Test
    fun `getVideoInfo throws exception for non-existing video id`() {
        MockWebServer().use { mockWebServer ->
            mockWebServer.dispatcher = dispatcher
            mockWebServer.start()

            val service = RealYouTubeService(TEST_API_KEY, mockWebServer.url("/").toString())
            assertThrows<IllegalArgumentException> { runBlocking { service.getVideoInfo("nonesuch") } }
        }
    }
}
