package hu.sarmin.yt2ig

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme

fun getUrlFrom(intent: Intent?): String? {
    if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    return null
}

class MainActivity : ComponentActivity() {
    private var sharedUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        this.sharedUrl = getUrlFrom(intent)

        setContent {
            Yt2igTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Display(
                        value = this.sharedUrl,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onNewIntent(newIntent: Intent, caller: ComponentCaller) {
        super.onNewIntent(newIntent, caller)
        setIntent(newIntent, caller)

        this.sharedUrl = getUrlFrom(newIntent)
    }
}

@Composable
fun Display(value: String?, modifier: Modifier = Modifier) {
    val displayed = value ?: "nothing shared"
    Text(
        text = displayed,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun DisplayPreview() {
    Yt2igTheme {
        Display("Android")
    }
}
