package com.enigmaticdevs.wallhaven.composeui

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun MainScreen(){
    Scaffold (
        topBar = {
           TopAppBar(
               navigationIcon = {
                   IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Icon"
                    )
                   }
               },
               title = {
                   Text(text = "Walldo")
               },
               colors = TopAppBarDefaults.smallTopAppBarColors(
                   containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
               ),

           )
        },
        content ={

        }
    )

}