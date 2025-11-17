package hu.sarmin.yt2ig

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import okhttp3.HttpUrl.Companion.toHttpUrl

private fun getUrlFrom(intent: Intent?): String? {
    if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    return null
}

private fun getStateFrom(intent: Intent?): State {
    try {
        val url = getUrlFrom(intent) ?: return State.Home
        val target = getTargetFor(url.toHttpUrl())

        if (target !is ValidShareTarget) {
            return State.Error("Unsupported share target")
        }

        return State.Preview(target)
    } catch (e: IllegalArgumentException) {
        return State.Error(e.message ?: "something went wrong")
    }
}

class MainActivity : ComponentActivity() {
    private val navStack = mutableStateListOf<State>()


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

    fun goHome() {
        this.navStack.add(State.Home)
    }
}
