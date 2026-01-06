package site.odintsov.booklog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.odintsov.booklog.R
import site.odintsov.booklog.data.Book

@Composable
fun InfoContent(book: Book) {
    Column(modifier = Modifier.padding(16.dp)) {
        InfoSection(label = stringResource(R.string.label_title), value = book.title)
        InfoSection(label = stringResource(R.string.label_authors), value = book.author)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.label_description),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = book.description ?: stringResource(R.string.no_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}