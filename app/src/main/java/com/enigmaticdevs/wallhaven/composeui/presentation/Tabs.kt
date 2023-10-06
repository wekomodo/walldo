package com.enigmaticdevs.wallhaven.composeui.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.data.model.Wallpapers

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tabs(contentPadding: PaddingValues, wallpaperList: Wallpapers?) {
    var state by remember { mutableStateOf(0) }
    val titles = listOf("Popular", "Latest")
    val pagerState = rememberPagerState(0)
    LaunchedEffect(state){
        pagerState.animateScrollToPage(state)
    }
    LaunchedEffect(pagerState.currentPage){
        state = pagerState.currentPage
    }
    Column(modifier = Modifier.padding(contentPadding)) {
        TabRow(selectedTabIndex = state,
           ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = state == index,
                    onClick = { state = index },
                    text = { Text(text = title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
        HorizontalPager(state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pageCount = titles.size) {index ->
                 when(index){
                     0->{
                         ComposeImageItem(photo = wallpaperList?.data ?: emptyList() )
                     }
                     1->{
                        Text(text = titles[index])
                     }
                 }
            
        }
    }
}


/*@Preview
@Composable
fun TabsPreview(){
    Tabs(contentPadding)
}*/
