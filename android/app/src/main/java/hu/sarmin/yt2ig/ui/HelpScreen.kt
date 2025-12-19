package hu.sarmin.yt2ig.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import hu.sarmin.yt2ig.R
import hu.sarmin.yt2ig.ui.util.PreviewScreenElement
import hu.sarmin.yt2ig.ui.util.ScrollIndicator
import hu.sarmin.yt2ig.ui.util.scrollFade

enum class HelpPage(val render: @Composable () -> Unit) {
    INTRO({ HelpIntro() }),
    YT_SHARING({ HelpYouTubeSharing() }),
    CREATION({ HelpCreation() }),
    LINK_STICKER({ HelpLinkSticker() }),
    FINAL({ HelpFinal() });
}

@Composable
fun HelpScreen(page: HelpPage = HelpPage.INTRO) {
    AppFrame(isHelp = true) { innerPadding ->
        StandardScreen(
            scrollable = false,
            modifier = Modifier
                .padding(innerPadding)
                .height(LocalConfiguration.current.screenHeightDp.dp)
        ) {
            HelpPager(page.ordinal)
        }
    }
}

@Composable
private fun HelpPager(initialPage: Int = 0) {
    val pagerState = rememberPagerState(
        pageCount = { HelpPage.entries.size },
        initialPage = initialPage
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val alpha = 1f - kotlin.math.abs(pageOffset).coerceIn(0f, 1f)

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .graphicsLayer {
                        this.alpha = alpha
                    }
            ) {
                val helpPageBackground = lerp(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.primary,
                    0.05f
                )

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = helpPageBackground,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    PageContent(page)
                }
            }
        }

        PageIndicator(
            pageCount = pagerState.pageCount,
            currentPage = pagerState.currentPage
        )
    }
}


@PreviewLightDark
@Composable
private fun PreviewHelp() {
    PreviewScreenElement {
        HelpPager()
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Box(modifier = Modifier
        .height(32.dp)
        .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { iteration ->
                val color = if (currentPage == iteration) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                }
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun PageContent(pageNumber: Int) {
    HelpPage.entries[pageNumber].render()
}

@Composable
private fun HelpPageCard(title: String, content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()
    val helpPageBackground = lerp(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.primary,
        0.05f
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .scrollFade(
                    scrollState = scrollState,
                    backgroundColor = helpPageBackground
                )
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val titleColor = lerp(
                MaterialTheme.colorScheme.onSurface,
                MaterialTheme.colorScheme.primary,
                0.15f
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                ),
                modifier = Modifier
                    .padding(bottom = 48.dp)
            )

            MaterialTheme(
                typography = MaterialTheme.typography.copy(
                    bodyMedium = MaterialTheme.typography.labelLarge,
                    bodySmall = MaterialTheme.typography.labelMedium
                )
            ) {
                content()
            }
        }

        if (scrollState.maxValue > 0) {
            ScrollIndicator(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            )
        }
    }
}

@Composable
private fun HelpIntro() {
    HelpPageCard("Share YouTube videos to this app") {
        Text("Share a link from YouTube, your browser, or any other app that can share text.")
        Text("Little Card will be there in your share menu.")

        val context = LocalContext.current
        val appIcon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            appIcon?.let { drawable ->
                Image(
                    bitmap = drawable.toBitmap(
                        width = 240,
                        height = 240,
                        config = android.graphics.Bitmap.Config.ARGB_8888
                    ).asImageBitmap(),
                    contentDescription = "Little Card App Icon",
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Text(
            "Don't worry too much about the format: even if there's extra text, Little Card will extract the video link automatically.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HelpYouTubeSharing() {
    HelpPageCard(
        "Sharing from the YouTube app"
    ) {
        Text("In the YouTube app, tap the Share button.")

        HelpImage("h1_yt_share", "YouTube share button highlighted")

        Text("You may need to tap 'More'.")

        HelpImage("h2_yt_more", "YouTube share sheet with More button highlighted")

        Text("That opens Android's full share list.", style = MaterialTheme.typography.bodySmall)
        Text("Find Little Card there.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun HelpCreation() {
    HelpPageCard(
        "Creating your card"
    ) {
        Text("Little Card prepares the image then.")
        Text("When the card appears, tap 'Share to Instagram Story'.")

        HelpImage("h4_littlecard", "Little Card share screen with Share to Instagram Story button highlighted")

        Text("You can also share the image to other apps.", style = MaterialTheme.typography.bodySmall)
    }

}

@Composable
private fun HelpLinkSticker() {
    HelpPageCard("Adding the link sticker") {
        Text("When the Instagram Story Editor opens, tap the sticker button at the top.")

        HelpImage("h6_insta_sticker_button", "Instagram Story editor with the Sticker button highlighted")

        Text("Choose the 'Link' sticker.")

        HelpImage("h7_insta_linksticker", "Instagram sticker panel with Link sticker highlighted")

        Text("The YouTube link is already in your clipboard.")
        Text("Paste it into the URL field.")
        
        HelpImage("h8_insta_link_pasted", "Instagram Link sticker editor with the YouTube URL pasted")
    }
}

@Composable
private fun HelpFinal() {
    HelpPageCard("Make it yours") {
        Text("Edit the Story however you like.")

        HelpImage("h9_insta_fin", "Instagram Story editor with the Little Card image rotated")

        Text("Your followers will be able to tap the link sticker to visit the YouTube video.")
    }
}

@Composable
private fun HelpImage(name: String, contentDescription: String) {
    val context = LocalContext.current

    val bitmap = remember(name) {
        try {
            val inputStream = context.assets.open("help/$name.png")
            val decodedBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            decodedBitmap
        } catch (e: Exception) {
            Log.e("HelpImage", "Failed to load help image: $name", e)
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Image not found:\n$name.png",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
