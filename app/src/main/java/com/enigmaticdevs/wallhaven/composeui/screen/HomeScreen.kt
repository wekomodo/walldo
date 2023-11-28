package com.enigmaticdevs.wallhaven.composeui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.enigmaticdevs.wallhaven.composeui.presentation.Tabs
import com.enigmaticdevs.wallhaven.composeui.presentation.TopAppBar
import com.enigmaticdevs.wallhaven.data.model.Category
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Purity
import com.enigmaticdevs.wallhaven.data.model.Sorting
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavController
){
    viewModel.getSearchWallpapers(
        "",
        Sorting.TOP_LIST.sort,
        "1y",
        Params(Purity.SFW.purity, Category.GENERAL.category, "", ""),
        1
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val wallpaperList  by viewModel.wallpaperSearchList.collectAsStateWithLifecycle()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { TopAppBar("Walldo", scrollBehavior = scrollBehavior,navController) },

            ) { contentPadding ->
            Tabs(contentPadding,wallpaperList.data)

        }
    }
}



