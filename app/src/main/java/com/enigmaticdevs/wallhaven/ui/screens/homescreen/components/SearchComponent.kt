package com.enigmaticdevs.wallhaven.ui.screens.homescreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enigmaticdevs.wallhaven.R
import com.enigmaticdevs.wallhaven.data.remote.model.Objects.Sorting
import com.enigmaticdevs.wallhaven.ui.util.searchInfoMessage
import kotlinx.coroutines.launch


// old way
/*

@Composable
fun SearchScreen(
    globalFilterRepository: GlobalFilterRepository,
    onBack: () -> Screen,
    onWallpaperClick: (String) -> Unit,
    viewModel: SearchViewModel = metroViewModel()
) {
    val textFieldState = rememberTextFieldState("")
    val activeSort by viewModel.activeSort.collectAsState()
    val pagingItems = viewModel.searchResults.collectAsLazyPagingItems()
     var showFilterSheet by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showFilterSheet = true
                }
            ) {
                Icon(imageVector = Icons.Rounded.Settings,"")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val context = LocalContext.current
            SimpleSearchBar(
                textFieldState = textFieldState,
                searchResults = listOf("Result 1", "Result 2", "Result 3"),
                onSearch = { query ->
                    viewModel.triggerSearch(query)
                    Toast.makeText(context, "$query searched", Toast.LENGTH_SHORT).show()
                },
                onBack = FocusRequester().freeFocus()
            )
            SortChipRow(
                currentSort = activeSort,
                onSortSelected = { newSort -> viewModel.updateSort(newSort) }
            )
            WallpaperListGrid(
                photos = pagingItems,
                onPhotoClick = onWallpaperClick,
                modifier = Modifier.weight(1f)
            )
            if(showFilterSheet)
                FilterBottomSheet(globalFilterRepository) {
                    showFilterSheet = false
                }
        }
    }
}
*/

@Composable
fun SimpleSearchBar(
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit
) {
    // Controls expansion state of the search bar
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    var showSearchBar by remember { mutableStateOf(true) }
    var showInfoDialog by remember { mutableStateOf(false) }
    //  val focusRequester = remember { FocusRequester() }

    // 2. Fire the focus request the moment this composable enters the screen
    /* LaunchedEffect(Unit) {
         focusRequester.requestFocus()
     }*/

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            //  modifier = Modifier.focusRequester(focusRequester),
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {
                onSearch(textFieldState.text.toString())
                scope.launch { searchBarState.animateToCollapsed() } // Closes the bar
            },
            trailingIcon = { // Only show the 'X' button if there is text in the field
                if (textFieldState.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            // Clears the text field
                            textFieldState.edit {
                                replace(0, length, "")
                                onSearch("")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Text"
                        )
                    }
                }
            },
            placeholder = {
                Text(modifier = Modifier.clearAndSetSemantics {}, text = "Search")
            },
        )
    }
    Box(
        modifier
            .fillMaxWidth()
            .padding(4.dp)
            .semantics {
                isTraversalGroup = true
            })
    {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            SearchBar(
                state = searchBarState,
                inputField = inputField,
                modifier = Modifier
                    .padding(start = 4.dp)
                    //.fillMaxWidth()
                    .semantics { traversalIndex = 0f }
                    .weight(1f)
            )
            FilledIconButton(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .padding(4.dp),
                onClick = onFilterClick
            ) {
                Icon(painter = painterResource(R.drawable.ic_filter_tune_v2), "Search Info")
            }
        }
        if (showInfoDialog)
            InfoAlertDialog { showInfoDialog = false }
        ExpandedFullScreenSearchBar(
            state = searchBarState,
            inputField = inputField,
        ) {
            // Display search results in a scrollable column
            Box(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    searchResults.forEach { result ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clickable {
                                    // Update the text field
                                    textFieldState.edit { replace(0, length, result) }
                                    // Trigger the search
                                    onSearch(result)
                                    // Animate the overlay closed
                                    scope.launch { searchBarState.animateToCollapsed() }
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            )
                            {
                                Text(result)
                                IconButton(
                                    onClick = {
                                        // delete the query from here
                                    }
                                ) {
                                    Icon(imageVector = Icons.Rounded.Clear, "clear saved query")

                                }
                            }

                        }
                    }
                }
                FloatingActionButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(
                            WindowInsets.ime.union(WindowInsets.navigationBars)
                        )
                        .padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Info, "Info")
                }
            }
        }

    }
}


@Preview
@Composable
fun SearchBarPreview() {
    val textFieldState = rememberTextFieldState("")
    SimpleSearchBar(
        textFieldState = textFieldState,
        onSearch = {},
        modifier = Modifier,
        searchResults = listOf("1"),
        onFilterClick = {}
    )
}

@Composable
fun InfoAlertDialog(
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Ways to find what you're looking for")
        },
        text = {
            Text(searchInfoMessage)
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Okay")
            }
        }
    )
}

