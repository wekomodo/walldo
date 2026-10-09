package com.wekomodo.huntshowdownwiki.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import androidx.compose.ui.geometry.Size as ComposeSize
import coil3.size.Size as CoilSize

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 3f,
    content: @Composable ZoomableBoxScope.() -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = modifier
            .clip(RectangleShape)
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = maxOf(minScale, minOf(scale * zoom, maxScale))
                    val maxX = (size.width * (scale - 1)) / 2
                    val minX = -maxX
                    offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                    val maxY = (size.height * (scale - 1)) / 2
                    val minY = -maxY
                    offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
                }
            }
    ) {
        val scope = ZoomableBoxScopeImpl(scale, offsetX, offsetY)
        scope.content()
    }
}

@Composable
fun ZoomableImage(image : Int,scale : Float, offsetX: Float,offsetY: Float){
    Image(
        painter = painterResource(id = image), contentDescription = "",
        modifier = Modifier
            .graphicsLayer(
                // adding some zoom limits (min 50%, max 200%)
                scaleX = maxOf(1f, minOf(3f, scale)),
                scaleY = maxOf(1f, minOf(3f, scale)),
                translationX = offsetX,
                translationY = offsetY
            )
            .fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
fun ZoomableAsyncImage(
    model: Any?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.FillWidth,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
    )
}

@Composable
fun ZoomableCoverImage(
    model: Any?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var intrinsic by remember { mutableStateOf(ComposeSize.Unspecified) }

    BoxWithConstraints(modifier) {
        val bounded = constraints.hasBoundedWidth && constraints.hasBoundedHeight
        val ready = bounded && intrinsic.isSpecified &&
                intrinsic.width > 0f && intrinsic.height > 0f

        val imageModifier = if (ready) {
            val cover = maxOf(
                constraints.maxWidth / intrinsic.width,
                constraints.maxHeight / intrinsic.height
            )
            val w = with(density) { (intrinsic.width * cover).toDp() }
            val h = with(density) { (intrinsic.height * cover).toDp() }
            Modifier
                .requiredSize(w, h)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        } else {
            Modifier.fillMaxSize()
        }

        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            onSuccess = { intrinsic = it.painter.intrinsicSize },
            modifier = imageModifier
        )
    }
}
interface ZoomableBoxScope {
    val scale: Float
    val offsetX: Float
    val offsetY: Float
}

private data class ZoomableBoxScopeImpl(
    override val scale: Float,
    override val offsetX: Float,
    override val offsetY: Float
) : ZoomableBoxScope