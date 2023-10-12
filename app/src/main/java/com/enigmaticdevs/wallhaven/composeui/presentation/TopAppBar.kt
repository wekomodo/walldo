package com.enigmaticdevs.wallhaven.composeui.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.enigmaticdevs.wallhaven.composeui.ui.theme.gorditaFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: String, scrollBehavior: TopAppBarScrollBehavior) {
    Column {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Icon"
                    )
                }
            },
            title = {
                Text(text = title,
                    fontFamily = gorditaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                )
            },
            actions = {
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search Icon"
                    )
                }
            },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopAppBarPreview(){

    TopAppBar(title = "walldo", TopAppBarDefaults.exitUntilCollapsedScrollBehavior())
}

