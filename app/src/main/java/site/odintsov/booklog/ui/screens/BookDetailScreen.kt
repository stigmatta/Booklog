package site.odintsov.booklog.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import site.odintsov.booklog.R
import site.odintsov.booklog.data.Book
import site.odintsov.booklog.ui.BookViewModel
import site.odintsov.booklog.ui.components.AddToLibraryDialog
import site.odintsov.booklog.ui.components.BookReviews
import site.odintsov.booklog.ui.components.GenreChip
import site.odintsov.booklog.ui.components.InfoContent
import site.odintsov.booklog.ui.components.StatusSection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BookDetailScreen(
    book: Book?,
    viewModel: BookViewModel,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    
    // Retrieve combined rating if available, else use book's default
    val combinedRatingPair = book?.let { viewModel.getCombinedRating(it.id) }
    val displayRating = combinedRatingPair?.first ?: book?.averageRating ?: 0.0
    val displayCount = combinedRatingPair?.second ?: book?.ratingsCount ?: 0

    LaunchedEffect(book?.id) {
        if (book != null) {
            viewModel.loadReviews(book)
        }
    }

    if (showDialog && book != null) {
        AddToLibraryDialog(
            onDismiss = { showDialog = false },
            onConfirm = { selectedStatus ->
                val updatedBook = book.copy(
                    status = selectedStatus,
                    isInLibrary = true,
                    readingProgress = if (selectedStatus == 2) 1.0f else 0.0f
                )
                viewModel.updateBookInLibrary(updatedBook)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (book != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Card(
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(width = 120.dp, height = 180.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        with(sharedTransitionScope) {
                            AsyncImage(
                                model = book.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .sharedElement(
                                        rememberSharedContentState(key = "img-${book.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row {
                            GenreChip(stringArrayResource(R.array.book_genres)[0])
                            Spacer(Modifier.width(4.dp))
                            GenreChip(stringArrayResource(R.array.book_genres)[1])
                        }
                    }
                }

                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { HorizontalDivider() }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.reading_status)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.tab_info)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(stringResource(R.string.tab_reviews)) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        if (book.isInLibrary) {
                            StatusSection(
                                initialStatus = book.status,
                                initialProgress = book.readingProgress,
                                initialRating = book.rating,
                                isInLibrary = book.isInLibrary,
                                onSave = { newStatus, newProgress, newRating ->
                                    val updatedBook = book.copy(
                                        status = newStatus,
                                        readingProgress = newProgress,
                                        rating = newRating,
                                        isInLibrary = true
                                    )
                                    viewModel.updateBookInLibrary(updatedBook)
                                    onBack()
                                },
                                onDelete = {
                                    val resetBook = book.copy(
                                        status = 0,
                                        isInLibrary = false,
                                        readingProgress = 0f,
                                        rating = 0
                                    )
                                    viewModel.updateBookInLibrary(resetBook)
                                    onBack()
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { showDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.btn_add_to_library),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    1 -> InfoContent(book)
                    2 -> BookReviews(
                        book = book.copy(averageRating = displayRating, ratingsCount = displayCount),
                        reviews = viewModel.reviews,
                        onAddReview = { rating, comment ->
                            viewModel.submitReview(book, rating, comment)
                        }
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.book_not_found))
            }
        }
    }
}
