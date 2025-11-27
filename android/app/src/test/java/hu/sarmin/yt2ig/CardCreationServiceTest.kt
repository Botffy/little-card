package hu.sarmin.yt2ig

import com.google.common.truth.Truth.assertThat
import hu.sarmin.yt2ig.testutil.EXISTING_VIDEO_ID
import hu.sarmin.yt2ig.testutil.YouTubeSimulator
import hu.sarmin.yt2ig.testutil.withDispatcher
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockWebServer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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

                assertState<AppState.Share.LoadingState.Created>(shareState) { createdState ->
                    assertThat(createdState.target.videoId).isEqualTo(EXISTING_VIDEO_ID)
                    assertThat(createdState.shareCard).isEqualTo(dummyShareCard)
                }
            }
        }
    }

    inline fun <reified T : AppState.Share.LoadingState> assertState(actual: AppState.Share, noinline block: (T) -> Unit) {
        assertThat(actual.loading).isInstanceOf(T::class.java)
        block(actual.loading as T)
    }
}
