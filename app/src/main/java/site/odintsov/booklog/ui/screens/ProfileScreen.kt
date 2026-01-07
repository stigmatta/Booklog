package site.odintsov.booklog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import site.odintsov.booklog.R
import site.odintsov.booklog.ui.BookViewModel
import site.odintsov.booklog.ui.components.AverageRatingChart
import site.odintsov.booklog.ui.components.ReadingChallengeCard
import site.odintsov.booklog.ui.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: BookViewModel,
    onNavigateToLibrary: () -> Unit,
    onBack: () -> Unit
) {
    val total by viewModel.totalBooks.observeAsState(0)
    val readCount by viewModel.readBooksCount.observeAsState(0)
    val wishlistCount by viewModel.wishlistCount.observeAsState(0)
    val readingCount by viewModel.readingCount.observeAsState(0)
    val avgRating by viewModel.averageRating.observeAsState(0f)
    val goalProgress by viewModel.goalProgress.observeAsState(0f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(R.string.my_challenge),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReadingChallengeCard(
                currentRead = readCount,
                goal = viewModel.yearlyGoal.intValue,
                progress = goalProgress
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.average_rating_stat),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AverageRatingChart(average = avgRating)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.overall_stats),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = stringResource(R.string.my_profile_overall), value = total.toString(), Modifier.weight(1f))
                StatCard(label = stringResource(R.string.status_isRead), value = readCount.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(label = stringResource(R.string.status_isReading), value = readingCount.toString(), Modifier.weight(1f))
                StatCard(label = stringResource(R.string.status_wishlist), value = wishlistCount.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onNavigateToLibrary,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.my_library))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

