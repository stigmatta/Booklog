package site.odintsov.booklog.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.odintsov.booklog.R
import site.odintsov.booklog.data.Book
import site.odintsov.booklog.ui.BookViewModel
import site.odintsov.booklog.ui.components.BookCardItem
import site.odintsov.booklog.ui.components.BookSearchBar
import site.odintsov.booklog.ui.components.BookTopAppBar
import site.odintsov.booklog.ui.components.GenreFilterBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    viewModel: BookViewModel,
    onBookClick: (Book) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
) {
    val books by viewModel.allBooks.observeAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading
    val selectedGenre by viewModel.selectedGenre
    val searchString = selectedGenre.lowercase()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val addedMessage = stringResource(R.string.added_to_library)

    var searchQuery by remember { mutableStateOf("") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.tab_info)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.dark_mode)) },
                    icon = {
                        Icon(
                            if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            null
                        )
                    },
                    badge = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onThemeToggle(it) }
                        )
                    },
                    selected = false,
                    onClick = { onThemeToggle(!isDarkTheme) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                BookTopAppBar(
                    titleRes = R.string.discover_books,
                    drawerState = drawerState,
                    scope = scope,
                    onProfileClick = { }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                BookSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { query ->
                        if (query.isNotEmpty()) {
                            viewModel.getPopularBooks(query)
                        }
                    }
                )

                GenreFilterBar(
                    genres = viewModel.genres,
                    selectedGenre = searchString,
                    onGenreSelected = { genre -> viewModel.onGenreSelected(genre) }
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(books.size) { index ->
                            val book = books.elementAt(index)
                            BookCardItem(
                                book = book,
                                onAdd = {
                                    viewModel.insert(book)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(addedMessage)
                                    }
                                },
                                onClick = { onBookClick(book) }
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}