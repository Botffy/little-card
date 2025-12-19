package hu.sarmin.yt2ig.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.sarmin.yt2ig.ui.util.ScrollIndicator
import hu.sarmin.yt2ig.ui.util.scrollFade

@Composable
fun StandardScreen(modifier: Modifier, scrollable: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    var scrollState: ScrollState? = null
    val modifierWithScrolling = if (scrollable) {
        scrollState = rememberScrollState()
        Modifier
            .scrollFade(
                scrollState,
                backgroundColor = MaterialTheme.colorScheme.background,
                fadeHeight = 8.dp
            )
            .verticalScroll(scrollState)

    } else {
        Modifier.fillMaxHeight()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = modifierWithScrolling
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }

        if ((scrollState?.maxValue ?: 0) > 0) {
            ScrollIndicator(
                scrollState = scrollState!!,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            )
        }
    }
}
