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
import hu.sarmin.yt2ig.util.DefaultHttpClientProvider
import hu.sarmin.yt2ig.util.hasInternet
import hu.sarmin.yt2ig.util.toHexRgb
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import java.io.File
import java.io.FileOutputStream

private fun getUrlFrom(intent: Intent?): String? {
    if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    return null
}

class MainActivity : ComponentActivity() {
    private val navStack = mutableStateListOf<AppState>()

    private val youTubeService: YouTubeService by lazy {
        RealYouTubeService(DefaultHttpClientProvider(), apiKey = BuildConfig.YOUTUBE_API_KEY)
    }

    private val imageLoader: ImageLoader by lazy { RealImageLoader(DefaultHttpClientProvider()) }
    private val imageStore: ImageStore by lazy { DefaultImageStore(this) }

    val cardCreationService: CardCreationService by lazy { CardCreationService(youTubeService, imageLoader, imageStore, Navigation(
        navigateTo = { newState -> navigateTo(newState) },
        replaceState = { oldState, newState -> replaceState(oldState, newState) }
    ))}

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
                this.navStack.lastOrNull() ?: AppState.Home,
                AppActions(
                    goHome = { goHome() },
                    parse = { parse(it) },
                    share = { share(it) },
                    shareToInstaStory = { shareToInstaStory(it.target.url, it.shareCard) },
                    shareToOther = { shareToOther(it.shareCard) },
                    copyUrl = { target -> copyToClipboard(target.url.toString()) },
                    toMessage = { errorMessage -> errorMessage.toMessage(this) }
                ),
                getContext = { this }
            )
        }
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        intent = newIntent
        handleIntent(newIntent)
    }

    private fun handleIntent(newIntent: Intent) {
        this.navStack.clear()
        val maybeUrl = getUrlFrom(newIntent)
        if (maybeUrl == null) {
            navigateTo(AppState.Home)
            return
        }

        when (val result = parse(maybeUrl))  {
            is Parsing.Error -> {
                navigateTo(AppState.Error(result.errorMessage, maybeUrl))
            }

            is Parsing.Result -> {
                share(result.target)
            }
        }
    }

    private fun share(target: ShareTarget.Valid) {
        // TODO this will get more generic, I promise
        if (target !is YouTubeVideo) {
            navigateTo(AppState.Error(ErrorMessage("error_parsing_unknownsharetarget"), target.url.toString()))
            return
        }

        if (!this.hasInternet()) {
            navigateTo(AppState.Error(ErrorMessage("error_no_network"), target.url.toString()))
            return
        }

        lifecycleScope.launch {
            cardCreationService.createCard(target)
        }
    }

    private fun navigateTo(newState: AppState) = this.navStack.add(newState)

    private fun replaceState(oldState: AppState, newState: AppState) {
        val index = this.navStack.indexOf(oldState)
        if (index != -1) {
            this.navStack[index] = newState
        }
    }

    fun goHome() = this.navStack.add(AppState.Home)

    private fun shareToInstaStory(url: HttpUrl, card: ShareCard) {
        val uri = writeFileToCache(card.image)
        copyToClipboard(url.toString())
        launchInsta(uri, card.gradientColors)
    }

    private fun writeFileToCache(bitmap: Bitmap): Uri {
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

    private fun shareToOther(card: ShareCard) {
        val uri = writeFileToCache(card.image)

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            setType("image/*")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share image via")
        startActivity(chooser)
    }

    private fun copyToClipboard(url: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Video URL", url)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
