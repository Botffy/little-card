package hu.sarmin.yt2ig.testutil

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import okhttp3.HttpUrl
import okio.Buffer

const val EXISTING_VIDEO_ID = "I_aBmrYChfQ"
const val TEST_API_KEY = "test-api-key"


open class YouTubeSimulator : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        return when {
            request.url.encodedPath == "/youtube/v3/videos" -> getVideo( request.url.queryParameter("id")!!)
            request.url.encodedPath.startsWith("/vi/") -> getThumbnail(request.url)
            else -> {
                MockResponse.Builder()
                    .code(404)
                    .body("Not Found")
                    .build()
            }
        }
    }

    open fun getVideo(videoId: String) : MockResponse {
        if (videoId == EXISTING_VIDEO_ID) {
            val responseBody = this.javaClass.getResource("/youtube_json/youtube_video_response.json")!!
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

    open fun getThumbnail(url: HttpUrl): MockResponse {
        val segments = url.encodedPathSegments
        val videoId = segments[1]
        val filename = segments[2]
        if (videoId == EXISTING_VIDEO_ID && filename in setOf("default.jpg", "mqdefault.jpg", "hqdefault.jpg")) {
            val imageData = this.javaClass.getResource("/images/sample.jpg")!!
                .readBytes()

            return MockResponse.Builder()
                .code(200)
                .body(Buffer().write(imageData))
                .addHeader("Content-Type", "image/jpeg")
                .build()
        } else {
            return MockResponse.Builder()
                .code(404)
                .body("Not Found")
                .build()
        }
    }
}
