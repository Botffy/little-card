package hu.sarmin.yt2ig

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import hu.sarmin.yt2ig.util.toHexRgb
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.io.FileOutputStream

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
            App(
                this.navStack.lastOrNull() ?: State.Home, fun() {
                    goHome()
                },
                shareToInstaStory = { shareToInstaStory(it.shareCard) }
            )
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
                    val loadedThumbnailState = State.Preview(target, PreviewLoadingState.LoadedThumbnail(videoInfo))
                    replaceState(loadedInfoState, loadedThumbnailState)

                    val shareCard = generateCard(videoInfo, imageLoader)
                    val createdPreviewState = State.Preview(
                        target,
                        PreviewLoadingState.CreatedPreview(videoInfo, shareCard)
                    )
                    replaceState(loadedThumbnailState, createdPreviewState)

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

    fun shareToInstaStory(card: ShareCard) {
        val uri = writeFileToCache(card.image)
        launchInsta(uri, card.gradientColors)
    }

    fun writeFileToCache(bitmap: Bitmap): Uri {
        cacheDir.listFiles { file -> file.name.startsWith("sharecard_") }?.forEach { it.delete() }

        val filename = "sharecard_${System.currentTimeMillis()}.png"
        val file = File(cacheDir, filename)
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }

        return FileProvider.getUriForFile(this,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )
    }

    fun launchInsta(imageUri: Uri, gradientColors: Pair<Int, Int>) {
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setType("image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("interactive_asset_uri", imageUri)
            putExtra("source_application", packageName)
            putExtra("top_background_color", gradientColors.first.toHexRgb())
            putExtra("bottom_background_color", gradientColors.second.toHexRgb())
            setPackage("com.instagram.android")
        }

        grantUriPermission("com.instagram.android", imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Instagram not installed", Toast.LENGTH_LONG).show()
        }
    }
}
