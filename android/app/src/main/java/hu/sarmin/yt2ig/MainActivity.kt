package hu.sarmin.yt2ig

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl

private fun getUrlFrom(intent: Intent?): String? {
    if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    return null
}

class MainActivity : ComponentActivity() {
    private val navStack = mutableStateListOf<State>()

    private val youTubeService: YouTubeService by lazy {
        RealYouTubeService(apiKey = BuildConfig.YOUTUBE_API_KEY)
    }

    private val imageLoader: ImageLoader by lazy { ImageLoader(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (this.navStack.isEmpty()) {
            handleIntent(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navStack.size > 1) {
                    navStack.removeAt(navStack.lastIndex)
                } else {
                    finish()
                }
            }
        })

        setContent {
            App(this.navStack.lastOrNull() ?: State.Home) { goHome() }
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        intent = newIntent

        handleIntent(newIntent)
    }

    private fun handleIntent(newIntent: Intent) {
        val newState = getStateFrom(newIntent)
        this.navStack.clear()
        this.navStack.add(newState)
    }

    private fun getStateFrom(intent: Intent?): State {

        try {
            val url = getUrlFrom(intent) ?: return State.Home
            val target = getTargetFor(url.toHttpUrl())

            // TODO this will get more generic, I promise
            if (target !is YouTubeVideo) {
                return State.Error("Unsupported share target")
            }

            val state = State.Preview(target, PreviewLoadingState.Loading)

            lifecycleScope.launch {
                try {
                    val videoInfo = youTubeService.getVideoInfo(target.videoId)
                    val loadedInfoState = State.Preview(target, PreviewLoadingState.LoadedInfo(videoInfo))
                    replaceState(state, loadedInfoState)

                    imageLoader.ensureLoaded(videoInfo.thumbnailUrl)
                    val loadedImageState = State.Preview(target, PreviewLoadingState.LoadedImage(videoInfo))
                    replaceState(loadedInfoState, loadedImageState)

                    val previewImage = generateCard(videoInfo, imageLoader)
                    val createdPreviewState = State.Preview(
                        target,
                        PreviewLoadingState.CreatedPreview(videoInfo, previewImage)
                    )
                    replaceState(loadedImageState, createdPreviewState)

                } catch (e: Exception) {
                    val errorState = State.Error(e.message ?: "something went wrong")
                    replaceState(state, errorState)
                }
            }

            return state
        } catch (e: IllegalArgumentException) {
            return State.Error(e.message ?: "something went wrong")
        }
    }

    private fun replaceState(oldState: State, newState: State) {
        val index = this.navStack.indexOf(oldState)
        if (index != -1) {
            this.navStack[index] = newState
        }
    }

    fun goHome() {
        this.navStack.add(State.Home)
    }

}
