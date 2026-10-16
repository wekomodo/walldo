package com.enigmaticdevs.wallhaven.ui.screens.settings

import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.imageLoader
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.navigation.Screen
import com.enigmaticdevs.wallhaven.ui.presentation.CategoryTitle
import com.enigmaticdevs.wallhaven.ui.presentation.ClickablePref
import com.enigmaticdevs.wallhaven.ui.presentation.PrefTopAppBar
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Screen,
    viewModel: SettingsViewModel = metroViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyCheck by viewModel.keyCheck.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackBarHostState.showSnackbar(it) }
    }

    // Close the dialog once the key is saved
    LaunchedEffect(keyCheck) {
        if (keyCheck == KeyCheck.Saved) {
            showApiKeyDialog = false
            viewModel.resetKeyCheck()
        }
    }



    Scaffold(
        topBar = {
            PrefTopAppBar("Settings") {
                onBack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CategoryTitle("Appearance")
            ClickablePref(
                text = "Theme",
                description = "Change the theme of the app",
                icon = R.drawable.ic_theme
            ) {
                showThemeDialog = true
            }


            HorizontalDivider()

            // Account
            CategoryTitle("Wallhaven Account")
            ClickablePref(
                text = "API key",
                description = when {
                    !state.isLoaded -> ""
                    state.apiKey.isBlank() -> "Required for NSFW filters"
                    else -> "Key added (tap to change)"
                },
                icon = R.drawable.ic_api_key
            ) {
                showApiKeyDialog = true
            }

            HorizontalDivider()

            CategoryTitle("Data and Storage")
            val size = Formatter.formatShortFileSize(context, state.cacheSizeBytes)
            ClickablePref(
                text = "Clear Image Cache",
                description = "Free up space :($size)",
                icon = R.drawable.ic_cleaning_services
            ) {
                viewModel.clearImageCache()
                scope.launch {
                    snackBarHostState.showSnackbar("Cache Cleared")
                }
            }


            CategoryTitle("About")
            ClickablePref(
                text = "Walldo",
                description = "Open-Source Wallhaven client (Github)",
                icon = R.drawable.wall
            ) {
                uriHandler.openUri("https://github.com/wekomodo/walldo")
            }

        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = "", // pass from ViewModel
            onSave = viewModel::authenticateAPIkey,
            keyCheck = keyCheck,
            onDismiss = {
                showApiKeyDialog = false
                viewModel.resetKeyCheck()
            }
        )
    }

}

@Composable
fun ApiKeyDialog(
    currentKey: String,
    onSave: (String) -> Unit,
    keyCheck: KeyCheck,
    onDismiss: () -> Unit
) {
    var keyText by remember { mutableStateOf(currentKey) }
    val checking = keyCheck == KeyCheck.Checking

    AlertDialog(
        onDismissRequest = { if (!checking) onDismiss() },
        title = { Text("Enter API Key") },
        text = {
            OutlinedTextField(
                value = keyText,
                onValueChange = { keyText = it },
                label = { Text("API Key") },
                singleLine = true,
                isError = keyCheck == KeyCheck.Invalid || keyCheck == KeyCheck.NetworkError,
                supportingText = {
                    when (keyCheck) {
                        KeyCheck.Invalid -> Text("Invalid API key")
                        KeyCheck.NetworkError -> Text("Couldn't reach Wallhaven. Check your connection.")
                        else -> {

                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    //Check if the key is valid or not
                    onSave(keyText)
                },
                enabled = !checking
            ) {
                if (checking) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !checking) { Text("Cancel") }
        }
    )
}

@Composable
fun Content() {


}