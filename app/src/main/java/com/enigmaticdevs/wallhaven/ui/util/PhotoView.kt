package com.enigmaticdevs.wallhaven.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Size as CoilSize
import kotlin.math.max
import kotlin.math.min

@Composable
fun PhotoView(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fillScreen: Boolean = false,
    maxZoom: Float = 4f,
    doubleTapZoom: Float = 2.5f,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val request = remember(model) {
        when (model) {
            is ImageRequest -> model.newBuilder()
            else -> ImageRequest.Builder(context).data(model)
        }.size(CoilSize(2048, 2048)).build()
    }
    val painter = rememberAsyncImagePainter(request)
    val intrinsic = painter.intrinsicSize

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val hasSize = intrinsic.isSpecified && intrinsic.width > 0f && intrinsic.height > 0f &&
            boxSize.width > 0 && boxSize.height > 0

    // Image size at zoom = 1 (fit or cover), in px
    val baseW: Float
    val baseH: Float
    if (hasSize) {
        val sx = boxSize.width / intrinsic.width
        val sy = boxSize.height / intrinsic.height
        val base = if (fillScreen) max(sx, sy) else min(sx, sy)
        baseW = intrinsic.width * base
        baseH = intrinsic.height * base
    } else {
        baseW = boxSize.width.toFloat()
        baseH = boxSize.height.toFloat()
    }

    // Keep the image edges from leaving the box (this is the part ZoomableBox got wrong)
    fun clamp(o: Offset, z: Float): Offset {
        val maxX = max(0f, (baseW * z - boxSize.width) / 2f)
        val maxY = max(0f, (baseH * z - boxSize.height) / 2f)
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .onSizeChanged { boxSize = it }
            .pointerInput(baseW, baseH, boxSize) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    val newZoom = (zoom * gestureZoom).coerceIn(1f, maxZoom)
                    val c = centroid - Offset(size.width / 2f, size.height / 2f)
                    // zoom around the fingers, then apply pan
                    val moved = c - (c - offset) * (newZoom / zoom) + pan
                    zoom = newZoom
                    offset = clamp(moved, newZoom)
                }
            }
            .pointerInput(baseW, baseH, boxSize) {
                detectTapGestures(onDoubleTap = { tap ->
                    if (zoom > 1.01f) {
                        zoom = 1f
                        offset = Offset.Zero
                    } else {
                        val target = min(maxZoom, doubleTapZoom)
                        val c = tap - Offset(size.width / 2f, size.height / 2f)
                        offset = clamp(c - (c - offset) * (target / zoom), target)
                        zoom = target
                    }
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds, // already sized to the right aspect ratio
            modifier = Modifier
                .requiredSize(
                    with(density) { baseW.toDp() },
                    with(density) { baseH.toDp() }
                )
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}