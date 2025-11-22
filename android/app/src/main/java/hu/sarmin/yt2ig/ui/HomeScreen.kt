package hu.sarmin.yt2ig.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import hu.sarmin.yt2ig.LocalAppActions
import hu.sarmin.yt2ig.ui.theme.Yt2igTheme
import hu.sarmin.yt2ig.ui.util.PreviewScreenElement

@Composable
fun HomeScreen() {
    val actions = LocalAppActions.current

    AppFrame(true, "yt2ig", { }) { padding ->
        StandardScreen(
            Modifier.padding(padding)
        ) {
            Intro()
            UrlInput(actions.onUrlEntered)

            Column {
                About()
                Links()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewHomeScreen() {
    Yt2igTheme {
        HomeScreen()
    }
}

@Composable
private fun Hi(style: TextStyle, color: Color) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = style.toSpanStyle()
                    .copy(color = color.copy(alpha = if (isSystemInDarkTheme()) 0.6f else 1f))
            ) { append("✨") }

            withStyle(
                style = style.toSpanStyle()
                    .copy(color = color)
            ) { append(" Hi!") }
        }
    )
}

@Composable
private fun Intro() {
    Card(
        modifier = Modifier
            .padding(bottom = 24.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Hi(
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Can I make a little card for you?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewIntro() {
    PreviewScreenElement {
        Intro()
    }
}

@Composable
private fun UrlInput(onUrlEntered: (maybeUrl: String) -> Unit = {}) {
    val text = remember {
        mutableStateOf("")
    }

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

            Text(
                text = "Paste a YouTube link!",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = true,
                value = text.value,
                onValueChange = { text.value = it },
                placeholder = {
                    Text("https://youtu.be/...")
                },
                singleLine = true,
            )

            Button(
                onClick = {
                    if (text.value.isNotBlank()) {
                        onUrlEntered(text.value.trim())
                    }

                },
                modifier = Modifier.align(Alignment.End),
                enabled = text.value.isNotBlank()
            ) {
                Text("Make my card")
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewUrlInput() {
    PreviewScreenElement {
        UrlInput()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Links(openTos: () -> Unit = {}, openPrivacy: () -> Unit = {}, toGithub: () -> Unit = {}) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TextButton(onClick = openTos)
            { Text("Terms of Service", style = MaterialTheme.typography.bodySmall) }
        TextButton(onClick = openPrivacy)
            { Text("Privacy Policy", style = MaterialTheme.typography.bodySmall) }
        TextButton(onClick = toGithub)
            { Text("Source code", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun About() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "About this app",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            val style = MaterialTheme.typography.bodySmall
                .copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

            val linkStyle = style.copy(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )

            Text(
                buildAnnotatedString {
                    withStyle(style.toSpanStyle()) {
                        append("""
                        yt2ig is a small application that helps you share YouTube videos in your Instagram Story.
                        
                        Share a YouTube link with this app, and it will generate a nice-looking card for you to share on Insta.
                        
                        If you're new here, check out our help page!
                        
                        - Not affiliated with YouTube or Instagram.
                        - Uses only public APIs, in compliance with their ToS.
                        - Does not store or collect any data about you.
                        
                        Any issues or feature requests? Drop me a line at  
                    """.trimIndent())
                    }

                    withLink(LinkAnnotation.Url("mailto:contact@sarmin-softworks.hu")) {
                        withStyle(linkStyle.toSpanStyle()) {
                            append("contact@sarmin-softworks.hu")
                        }
                    }
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewAbout() {
    PreviewScreenElement {
        About()
    }
}
