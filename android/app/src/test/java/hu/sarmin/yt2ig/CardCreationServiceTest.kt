package hu.sarmin.yt2ig

import com.google.common.truth.Truth.assertThat
import hu.sarmin.yt2ig.testutil.EXISTING_VIDEO_ID
import hu.sarmin.yt2ig.testutil.YouTubeSimulator
import hu.sarmin.yt2ig.testutil.withDispatcher
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.HttpUrl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class CardCreationServiceTest {
    private val appState = FakeNavigation()

    private val dummyShareCard = mockk<ShareCard>()
    init {
        mockkStatic(::generateCard)
        coEvery {
            generateCard(any(), any(), any())
        } returns dummyShareCard
    }

    @BeforeEach
    fun setup() {
        appState.reset()
    }

    fun service(clientProvider: MockWebServerHttpClientProvider): CardCreationService {
        return CardCreationService(
            youTubeService = RealYouTubeService(clientProvider, "dummy-api-key"),
            imageStore = FakeImageStore(),
            imageLoader = RealImageLoader(clientProvider),
            navigation = appState.navigation
        )
    }

    @Test
    fun happyDay() {
        MockWebServer().withDispatcher(YouTubeSimulator()) { _, httpClientProvider ->
            val service = service(httpClientProvider)

            runBlocking {
                service.createCard(YouTubeVideo(EXISTING_VIDEO_ID))
            }

            assertThat(appState.navStack.size).isEqualTo(1)
            appState.navStack[0].let {
                assertThat(it).isInstanceOf(AppState.Share::class.java)
                val shareState = it as AppState.Share

                assertShareState<AppState.Share.LoadingState.Created>(shareState) { createdState ->
                    assertThat(createdState.target.videoId).isEqualTo(EXISTING_VIDEO_ID)
                    assertThat(createdState.shareCard).isEqualTo(dummyShareCard)
                }
            }
        }
    }

    @Test
    fun `nonexistent ID leads to error`() {
        MockWebServer().withDispatcher(YouTubeSimulator()) { _, httpClientProvider ->
            val service = service(httpClientProvider)

            runBlocking {
                service.createCard(YouTubeVideo("non-existing-video-id"))
            }

            assertThat(appState.navStack.size).isEqualTo(1)
            assertError(appState.navStack[0], YouTubeCardCreationError.VideoInfoNotFound)
        }
    }

    @Test
    fun `thumbnail not found leads to error`() {
        MockWebServer().withDispatcher(object : YouTubeSimulator() {
            override fun getThumbnail(url: HttpUrl): MockResponse {
                return MockResponse().newBuilder().code(404).build()
            }
        }) { _, httpClientProvider ->
            val service = service(httpClientProvider)

            runBlocking {
                service.createCard(YouTubeVideo(EXISTING_VIDEO_ID))
            }

            assertThat(appState.navStack.size).isEqualTo(1)
            assertError(appState.navStack[0], ImageLoaderError.ImageDownloadFailed)
        }
    }

    @Test
    fun `YouTube timeouting`() {
        MockWebServer().withDispatcher(object : YouTubeSimulator() {
            override fun getVideo(videoId: String): MockResponse {
                return MockResponse().newBuilder().headersDelay(31, TimeUnit.SECONDS).build()
            }
        }) { _, httpClientProvider ->
            val service = service(httpClientProvider)

            runBlocking {
                service.createCard(YouTubeVideo(EXISTING_VIDEO_ID))
            }

            assertThat(appState.navStack.size).isEqualTo(1)
            assertError(appState.navStack[0], ErrorMessage("error_network_timeout"))
        }
    }

    inline fun <reified T : AppState.Share.LoadingState> assertShareState(actual: AppState.Share, noinline block: (T) -> Unit) {
        assertThat(actual.loading).isInstanceOf(T::class.java)
        block(actual.loading as T)
    }

    fun assertError(state: AppState, expected: ErrorMessage) {
        assertThat(state).isInstanceOf(AppState.Error::class.java)
        val errorState = state as AppState.Error
        assertThat(errorState.error).isEqualTo(expected)
    }
    fun assertError(state: AppState, expected: CardCreationError) {
        assertError(state, expected.errorMessage)
    }
}
