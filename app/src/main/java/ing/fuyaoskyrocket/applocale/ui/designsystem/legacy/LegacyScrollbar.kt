package ing.fuyaoskyrocket.applocale.ui.designsystem.legacy

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun LegacyVerticalScrollBar(
    drawable: android.graphics.drawable.Drawable,
    fadeDelayMillis: Long,
    fadeDurationMillis: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var active by remember { mutableStateOf(false) }
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            active = true
        } else {
            delay(fadeDelayMillis)
            active = false
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(fadeDurationMillis),
        label = "legacyScrollbarFade",
    )
    if (alpha <= 0.01f) return

    val geometry by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            if (info.visibleItemsInfo.isEmpty()) {
                0f to 1f
            } else {
                val first = info.visibleItemsInfo.first()
                val average = info.visibleItemsInfo
                    .sumOf { it.size }.toFloat() / info.visibleItemsInfo.size
                val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
                val total = info.totalItemsCount
                val content = total * average
                val maxScroll = (content - viewport).coerceAtLeast(1f)
                val scrolled = (first.index * average - first.offset).coerceAtLeast(0f)
                val offsetFraction = (scrolled / maxScroll).coerceIn(0f, 1f)
                // The handle covers the visible fraction, at minimum a short stub.
                val lengthFraction = (viewport / content).coerceIn(0.08f, 1f)
                offsetFraction to lengthFraction
            }
        }
    }
    val (offsetFraction, lengthFraction) = geometry
    val handleWidth = with(density) {
        (drawable.intrinsicWidth.takeIf { it > 0 } ?: 8).toDp()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(end = 2.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        val trackHeightPx = constraints.maxHeight.toFloat()
        Box(
            modifier = Modifier
                .fillMaxHeight(lengthFraction)
                .width(handleWidth)
                .alpha(alpha)
                .graphicsLayer {
                    translationY = offsetFraction * (trackHeightPx * (1f - lengthFraction))
                }
                .legacyDrawable(drawable, alignment = Alignment.TopCenter),
        )
    }
}
