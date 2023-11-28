package com.enigmaticdevs.wallhaven.composeui.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enigmaticdevs.wallhaven.data.model.Wallpaper



@Composable
fun ComposeImageItem(
    photo: List<Wallpaper>,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
    )
    {
        Log.d("COMPOSE", photo.toString())
        photo.forEach {
            item {
                val width = it.dimension_x
                val height = it.dimension_y
                val aspectRatio = width.toDouble() / height.toDouble()
                AsyncImage(
                    model = it.thumbs.original,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(aspectRatio.toFloat())
                        .defaultMinSize(minHeight = 100.dp)
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }

}