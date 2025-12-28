package hu.sarmin.yt2ig.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_OFF
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import hu.sarmin.yt2ig.AppState
import hu.sarmin.yt2ig.LocalAppActions
import hu.sarmin.yt2ig.Parsing
import hu.sarmin.yt2ig.ShareTarget
import hu.sarmin.yt2ig.ui.common.SpeedbumpModal
import hu.sarmin.yt2ig.ui.common.TextWithEmoji
import hu.sarmin.yt2ig.ui.common.UrlInput
import hu.sarmin.yt2ig.ui.common.UrlInputInitialValue
import hu.sarmin.yt2ig.ui.theme.MonoFont
import hu.sarmin.yt2ig.ui.util.PreviewScreenElement

@Composable
fun HomeScreen(data: AppState.Home.Data) {
    val actions = LocalAppActions.current
    AppFrame(isHome = true) { padding ->
        StandardScreen(
            Modifier.padding(padding)
        ) {
            Intro()
            UrlInput(
                initialValue = if (data is AppState.Home.Data.WithClipboardData && !data.clipboardData.isValid()) UrlInputInitialValue.Parsed(data.clipboardData) else null,
                label = "Paste a link!",
                buttonLabel = "Make my card",
                parse = actions.parse,
                share = actions.share,
                errorMessageConverter = actions.toMessage
            )

            Column {
                About()
                Links()
            }

            if (data is AppState.Home.Data.WithClipboardData && data.clipboardData.parsing is Parsing.Result) {
                ClipboardModalDialog(
                    shareTarget = data.clipboardData.parsing.target,
                    onDismiss = {
                        actions.clearHomeClipboard()
                    },
                    onConfirm = { parsed ->
                        actions.clearHomeClipboard()
                        actions.share(parsed)
                    }
                )
            }
        }
    }
}

@Composable
fun ClipboardModalDialog(
    shareTarget: ShareTarget.Valid,
    onDismiss: () -> Unit,
    onConfirm: (target: ShareTarget.Valid) -> Unit
) {
    SpeedbumpModal(
        onDismiss = onDismiss,
        onConfirm = { onConfirm(shareTarget) },
        icon = Icons.Outlined.ContentPasteGo,
        topTitle = "There's a link on your clipboard",
        mainTitleContent = {
            Text(
                text = shareTarget.displayUrl,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = MonoFont,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.2.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        confirmButtonText = "Create card!",
        scrimAlpha = 0.5f
    ) {
        Text(
            text = "Would you like me to make a card for it?"
        )
    }
}

@Composable
private fun Hi(style: TextStyle, color: Color) {
    TextWithEmoji(
        text = "Hi!",
        emoji = "✨",
        style = style,
        color = color,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Links() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val context = LocalContext.current
        val colorScheme = MaterialTheme.colorScheme
        val open = { uri: String ->
            openInWebTab(uri.toUri(), context, colorScheme)
        }

        TextButton(onClick = { open("https://botffy.github.io/little-card/tos.html") })
            { Text("Terms of Service", style = MaterialTheme.typography.bodySmall) }
        TextButton(onClick = { open("https://botffy.github.io/little-card/privacy.html") })
            { Text("Privacy Policy", style = MaterialTheme.typography.bodySmall) }
        TextButton(onClick = { openInExternalBrowser("https://github.com/Botffy/little-card/".toUri(), context) })
            { Text("Source code", style = MaterialTheme.typography.bodySmall) }
    }
}

private fun openInWebTab(uri: Uri, context: Context, colorScheme: ColorScheme) {
    val embeddedTab = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
            .setToolbarColor(colorScheme.primary.toArgb())
            .build())
        .setShareState(SHARE_STATE_OFF)
        .setShowTitle(true)
        .setEphemeralBrowsingEnabled(true)
        .build()

    embeddedTab.intent.putExtra(
        Intent.EXTRA_REFERRER,
        ("android-app://" + context.packageName).toUri()
    )

    embeddedTab.launchUrl(context, uri)
}

private fun openInExternalBrowser(uri: Uri, context: Context) {
    val intent = Intent(android.content.Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
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
                        This is a small application that helps you share YouTube videos or YouTube Music songs in your Instagram Story.
                        
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
