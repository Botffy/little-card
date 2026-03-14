package hu.sarmin.yt2ig.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import hu.sarmin.yt2ig.LocalAppActions
import hu.sarmin.yt2ig.ParsedText
import hu.sarmin.yt2ig.Parsing
import hu.sarmin.yt2ig.ui.common.TextWithEmoji
import hu.sarmin.yt2ig.ui.common.UrlInput
import hu.sarmin.yt2ig.ui.util.PreviewScreenElement


@Composable
fun ErrorScreen(message: String, initialValue: ParsedText?, goHome: () -> Unit) {
    val actions = LocalAppActions.current

    AppFrame { padding ->
        StandardScreen(
            Modifier.padding(padding)
        ) {
            ErrorMessage(
                text = message
            )

            val url = rememberSaveable(initialValue) { mutableStateOf(initialValue?.text ?: "") }
            val error = rememberSaveable(initialValue, saver = ErrorStateSaver) {
                mutableStateOf(null as Pair<String, Parsing.Error>?)
            }

            UrlInput(
                value = url.value,
                error = error.value,
                onValueChange = {
                    if (it != error.value?.first) {
                        error.value = null
                    }
                    url.value = it
                },
                onSubmit = {
                    val parsingResult = actions.parse(url.value)

                    actions.updateErrorInput(ParsedText(url.value, parsingResult))
                    if (parsingResult is Parsing.Result) {
                        error.value = null
                        actions.share(url.value, parsingResult.target)
                    } else if (parsingResult is Parsing.Error) {
                        error.value = url.value to parsingResult
                    }
                },
                label = "The URL you tried:",
                buttonLabel = "Retry",
                errorMessageConverter = actions.toMessage,
            )
        }
    }
}

@Composable
private fun ErrorMessage(text: String) {
    Card(
        modifier = Modifier
            .padding(bottom = 24.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ErrorStripe()

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                TextWithEmoji(
                    emoji = "\uD83D\uDE14",
                    text = "Uh-oh",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ErrorStripe()
        }
    }
}

@Composable
private fun ErrorStripe() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(MaterialTheme.colorScheme.error)
    )
}

@PreviewLightDark
@Composable
private fun PreviewErrorMessage() {
    PreviewScreenElement {
        ErrorMessage("Could not find video info! Are you sure your link is correct?")
    }
}
