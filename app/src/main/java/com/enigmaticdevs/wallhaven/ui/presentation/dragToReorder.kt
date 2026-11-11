package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.State
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex

@Stable
class DragDropState(
    private val lazyListState: LazyListState,
    private val onMove: State<(Int, Int) -> Unit>,
    private val onDragDone: State<() -> Unit>
) {
    var draggedKey by mutableStateOf<Any?>(null)
        private set

    private var initialOffset by mutableIntStateOf(0)
    private var distance by mutableFloatStateOf(0f)

    // Derived from live layout info, so it stays correct after swaps
    val draggedOffset: Float
        get() {
            val item = lazyListState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == draggedKey } ?: return 0f
            return initialOffset + distance - item.offset
        }

    fun onDragStart(key: Any) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == key }
            ?.let {
                draggedKey = it.key
                initialOffset = it.offset
                distance = 0f
            }
    }

    fun onDrag(deltaY: Float) {
        distance += deltaY
        val infos = lazyListState.layoutInfo.visibleItemsInfo
        val current = infos.firstOrNull { it.key == draggedKey } ?: return

        val center = initialOffset + distance + current.size / 2f
        val target = infos.firstOrNull {
            it.index != current.index &&
                    center >= it.offset && center <= it.offset + it.size
        } ?: return

        val firstIndex = lazyListState.firstVisibleItemIndex
        val firstOffset = lazyListState.firstVisibleItemScrollOffset

        onMove.value(current.index, target.index)

// Stop the list from following the displaced first item
        if (current.index == firstIndex || target.index == firstIndex) {
            lazyListState.requestScrollToItem(firstIndex, firstOffset)
        }
    }

    fun onDragEnd() {
        draggedKey = null
        distance = 0f
        onDragDone.value()
    }
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit,
    onDragDone: () -> Unit
): DragDropState {
    val move = rememberUpdatedState(onMove)
    val done = rememberUpdatedState(onDragDone)
    return remember(lazyListState) { DragDropState(lazyListState, move, done) }
}

fun Modifier.dragHandle(dragDropState: DragDropState, key: Any): Modifier =
    this.pointerInput(dragDropState, key) {
        detectDragGestures(
            onDragStart = { dragDropState.onDragStart(key) },
            onDrag = { change, amount ->
                change.consume()
                dragDropState.onDrag(amount.y)
            },
            onDragEnd = { dragDropState.onDragEnd() },
            onDragCancel = { dragDropState.onDragEnd() }
        )
    }

@Composable
fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    key: Any,
    content: @Composable (isDragging: Boolean, handleModifier: Modifier) -> Unit
) {
    val isDragging = key == dragDropState.draggedKey
    val modifier = if (isDragging) {
        Modifier.zIndex(1f).graphicsLayer {
            translationY = dragDropState.draggedOffset
            scaleX = 1.03f
            scaleY = 1.03f
        }
    } else Modifier.animateItem()

    Box(modifier) {
        content(isDragging, Modifier.dragHandle(dragDropState, key))
    }
}