package hu.sarmin.yt2ig.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hu.sarmin.yt2ig.AppState
import hu.sarmin.yt2ig.LocalAppActions
import hu.sarmin.yt2ig.ShareTarget
import hu.sarmin.yt2ig.ui.common.SpeedbumpModal
import hu.sarmin.yt2ig.ui.util.BulletItem

@Composable
fun SharingScreen(target: ShareTarget.Valid, loading: AppState.Share.LoadingState) {
    val actions = LocalAppActions.current
    AppFrame(helpPage = HelpPage.CREATION) { padding ->
        StandardScreen(
            Modifier.padding(padding)
        ) {
            val statusText = when (loading) {
                is AppState.Share.LoadingState.Starting -> "Loading info for: ${target.url}"
                is AppState.Share.LoadingState.LoadedInfo -> "Downloading thumbnail for '${loading.data.title}'"
                is AppState.Share.LoadingState.LoadedThumbnail -> "Creating share card for '${loading.data.title}'"
                is AppState.Share.LoadingState.Created -> "Done"
            }

            val openReminderDialog = remember { mutableStateOf(false) }

            if (openReminderDialog.value) {
                LinkReminderModal(
                    onDismiss = { openReminderDialog.value = false },
                    onConfirm = {
                        openReminderDialog.value = false
                        if (loading !is AppState.Share.LoadingState.Created) {
                            Log.e("SharingScreen", "The image isn't ready during sharing!")
                            return@LinkReminderModal
                        }
                        actions.shareToInstaStory(loading)
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Crossfade(
                                targetState = loading as? AppState.Share.LoadingState.Created,
                                animationSpec = tween(durationMillis = 1000)
                            ) { created ->
                                when (created) {
                                    null -> ShareLoadingBox(statusText)
                                    else -> LoadedImage(created.shareCard.image)
                                }
                            }

                            Spacer(modifier = Modifier.size(16.dp))

                            ShareButtons(
                                enabled = loading is AppState.Share.LoadingState.Created,
                                shareToInsta = {
                                    when (loading) {
                                        is AppState.Share.LoadingState.Created -> {
                                            openReminderDialog.value = true
                                        }
                                        else -> {}
                                    }
                                },
                                shareToOther = {
                                    when (loading) {
                                        is AppState.Share.LoadingState.Created -> actions.shareToOther(loading)
                                        else -> {}
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(32.dp))

                    UrlBlock(target.url.toString()) {
                        actions.copyUrl(target)
                    }

                    Spacer(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun LinkReminderModal(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    SpeedbumpModal(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        icon = Icons.Outlined.Link,
        topTitle = "We'll copy the link for you",
        mainTitle = "Add a Link sticker in Instagram",
        confirmButtonText = "Copy link & open Instagram"
    ) {
        Text(text = "After the Instagram Story editor opens:")

        Spacer(Modifier.height(12.dp))

        BulletItem(
            buildAnnotatedString {
                append("Tap the ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Sticker")
                }
                append(" button")
            }
        )
        Spacer(Modifier.height(6.dp))
        BulletItem(
            buildAnnotatedString {
                append("Choose the ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Link")
                }
                append(" sticker")
            }
        )
        Spacer(Modifier.height(6.dp))
        BulletItem(
            buildAnnotatedString {
                append("Paste the copied URL into the ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("link field")
                }
            }
        )
    }
}

@Composable
private fun ShareLoadingBox(statusText: String) {
    Box(
        modifier = Modifier
            .heightIn(min = 60.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(strokeWidth = 3.dp)
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewShareLoading() {
    Box(Modifier.size(360.dp)) {
        ShareLoadingBox("Loading preview...")
    }
}

@Composable
private fun LoadedImage(image: Bitmap) {
    Image(
        bitmap = image.asImageBitmap(),
        contentDescription = "Image",
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp, top = 12.dp),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
private fun ShareButtons(enabled: Boolean, shareToInsta: () -> Unit = {}, shareToOther: () -> Unit = {}) {

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = shareToInsta,
            enabled = enabled,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null
            )
            Spacer(
                modifier = Modifier.width(8.dp)
            )
            Text("Share to Insta Story!")
        }

        OutlinedButton(
            onClick = shareToOther,
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun UrlBlock(url: String, onCopy: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Video link to share",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SelectionContainer {
                    Text(
                        text = url,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }

                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy link"
                    )
                }
            }
        }
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewUrlBlock() {
    Box(
        modifier = Modifier
            .size(360.dp)
    ) {
        UrlBlock("http://youtu.be/videoid") { }
    }
}
