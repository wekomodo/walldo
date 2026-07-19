package com.enigmaticdevs.wallhaven.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.data.model.Wallpapers
import com.enigmaticdevs.wallhaven.data.model.local.TabItem
import com.enigmaticdevs.wallhaven.ui.screens.SpacerX

@Composable
fun Tabs(
    contentPaddingValues: PaddingValues,
    wallpapersList: Wallpapers
) {
    val titles = listOf<TabItem>(TabItem("Popular",Icons.Rounded.Star), TabItem("Recent",Icons.Rounded.DateRange))
    var state by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        titles.size
    }
    LaunchedEffect(state){
        pagerState.animateScrollToPage(state)
    }
    LaunchedEffect(pagerState.currentPage){
        state = pagerState.currentPage
    }
    Column(modifier = Modifier.padding(contentPaddingValues)) {
        PrimaryTabRow(selectedTabIndex = state) {
            titles.forEachIndexed { index, item ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = {

                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = "Popular"
                                )
                                Text(
                                    modifier = Modifier.padding(start = 5.dp),
                                    text = item.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                    }
                )
            }
        }
        SpacerX(4)
        HorizontalPager(state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f)) { index ->
            when(index){
                0->{
                    Text("Popular")
                }
                1->{
                    Text("Latest")
                }
            }
        }
    }
}