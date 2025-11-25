package hu.sarmin.yt2ig

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CardCreationServiceTest {
    val youTubeService = FakeYouTubeService()
    val imageLoader = FakeImageLoader()
    val appState = FakeNavigation()

    private fun service(): CardCreationService = CardCreationService(youTubeService, imageLoader, appState.navigation)

    val dummyShareCard = mockk<ShareCard>()
    init {
        mockkStatic(::generateCard)
        coEvery {
            generateCard(any(), any())
        } returns dummyShareCard
    }

    @BeforeEach
    fun setUp() {
        youTubeService.reset()
        imageLoader.reset()
        appState.reset()
    }

    @Test
    fun happyDay() {
        val service = service()

        youTubeService.addVideoInfo("nevergon", YouTubeVideoInfo(
            title = "Never Gonna Give You Up",
            channel = "Rick Astley",
            thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg".toHttpUrl()
        ))
        imageLoader.addImage("https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg".toHttpUrl())

        runBlocking {
            service.createCard(YouTubeVideo("nevergon"))
        }

        assertThat(appState.navStack.size).isEqualTo(1)
        appState.navStack[0].let {
            assertThat(it).isInstanceOf(AppState.Share::class.java)
            val shareState = it as AppState.Share
            assertThat(shareState.loading).isInstanceOf(AppState.Share.LoadingState.Created::class.java)
        }
    }
}
