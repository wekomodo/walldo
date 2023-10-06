package com.enigmaticdevs.wallhaven.composeui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enigmaticdevs.wallhaven.composeui.presentation.Tabs
import com.enigmaticdevs.wallhaven.composeui.presentation.TopAppBar
import com.enigmaticdevs.wallhaven.composeui.ui.theme.AppTheme
import com.enigmaticdevs.wallhaven.data.model.Category
import com.enigmaticdevs.wallhaven.data.model.Params
import com.enigmaticdevs.wallhaven.data.model.Purity
import com.enigmaticdevs.wallhaven.data.model.Sorting
import com.enigmaticdevs.wallhaven.domain.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getSearchWallpapers(
            "",
            Sorting.TOP_LIST.sort,
            "1y",
            Params(Purity.SFW.purity, Category.GENERAL.category, "", ""),
            1
        )

        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                    val wallpaperList  by viewModel.wallpaperSearchList.collectAsStateWithLifecycle()
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = { TopAppBar("Walldo", scrollBehavior = scrollBehavior) },

                    ) { contentPadding ->
                        Tabs(contentPadding,wallpaperList.data)

                    }
                }
            }
        }
    }
}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WallhavenTheme {
        MainScreen()
    }
}*/
