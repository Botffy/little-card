package hu.sarmin.yt2ig.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import hu.sarmin.yt2ig.ErrorMessage
import hu.sarmin.yt2ig.ParsedText
import hu.sarmin.yt2ig.Parsing
import hu.sarmin.yt2ig.ui.util.PreviewScreenElement


sealed interface UrlInputInitialValue {
    val text: String

    data class Parsed(val parsedText: ParsedText) : UrlInputInitialValue {
        override val text: String get() = parsedText.text
        val parsing: Parsing get() = parsedText.parsing
        fun toErrorPair(): Pair<String, Parsing.Error>? {
            return if (parsing is Parsing.Error) {
                text to parsing as Parsing.Error
            } else {
                null
            }
        }
    }
    data class Raw(override val text: String) : UrlInputInitialValue
}

@Composable
fun UrlInput(
    value: String = "",
    error: Pair<String, Parsing.Error>? = null,
    onValueChange: (String) -> Unit = {},
    label: String? = null,
    buttonLabel: String = "Make my card",
    onSubmit: () -> Unit = {},
    errorMessageConverter: (ErrorMessage) -> String = { it.code },
) {

    Card(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
                
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = true,
                value = value,
                onValueChange = { onValueChange(it) },
                placeholder = {
                    Text("https://youtu.be/...")
                },
                singleLine = true,
                isError = error != null
            )

            error?.let { value ->
                TextWithEmoji(
                    text =  errorMessageConverter(value.second.errorMessage),
                    emoji = "⚠️",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { onSubmit() },
                modifier = Modifier.align(Alignment.End),
                enabled = value.isNotBlank()
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewUrlInput() {
    PreviewScreenElement {
        UrlInput(
            label = "This is a label!",
            buttonLabel = "Custom button",
        )
    }
}
